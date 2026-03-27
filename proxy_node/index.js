const http = require("http");
const httpProxy = require("http-proxy");
const Redis = require("ioredis");

const redisUrl = process.env.REDIS_URL || 'redis://redis-service:6379'  // our proxy is also in k8s cluster 
                                                                        // hence calling by service name

const redis = new Redis(redisUrl, {
    maxRetriesPerRequest: null,
    enableReadyCheck: false,
    retryStrategy(times) {
        const delay = Math.min(times * 50, 2000); // Backoff strategy
        console.log(`Redis connection failed. Retrying in ${delay}ms...`);
        return delay;
    }
});

redis.on('error', (err) => {
    console.error('Redis Client Error:', err.message);
});

redis.on('connect', () => {
    console.log('✅ Connected to Redis successfully');
});

const proxy = httpProxy.createProxyServer({
    ws: true,  // websockets
    xfwd: true,  // header forwarding
    changeOrigin: true
});


async function getTarget(hostname) {
    try {
        const targetIp = await redis.get(`route:${hostname}`);  // key = 'route:project-id.app.devvvotee.com'
        if (targetIp) {
            return targetIp;
        }
    } catch (err) {
        console.error('Redis Error:', err);
    }
    return null;
}

// HELPER: Ensure target has the correct format
const getTargetUrl = (ip) => {
    return `http://${ip}`;
};

const server = http.createServer(async (req, res) => {
    const rawHost = req.headers.host || ''; // project-id.app.devvvotee.com:8090
    const hostname = rawHost.split(':')[0]; // project-id.app.devvvotee.com

    const targetIp = await getTarget(hostname);

    if (!targetIp) {
        res.writeHead(404, { 'Content-Type': 'text/plain' });
        return res.end(`Preview not found for ${hostname}.`);
    }

    const target = getTargetUrl(targetIp); // http://10.244.0.7:5173
    console.log(`HTTP Proxy: ${hostname} -> ${target}${req.url}`);


    // this will forward our request and catch error if any
    proxy.web(req, res, { target }, (e) => {
        console.error(`Proxy Error (Web): ${hostname}`, e.message);
        if (!res.headersSent) {
            res.writeHead(502);
            res.end('Vite server unavailable...');
        }
    });
});


// this is for hmr, so that vite server and our frontend stays connected over a websocket to listen to updates
// otherwise there will be need to reload the page manually everytime code changes

server.on('upgrade', async (req, socket, head) => {
    const rawHost = req.headers.host || '';
    const hostname = rawHost.split(':')[0];

    const targetIp = await getTarget(hostname);

    if (targetIp) {
        const target = getTargetUrl(targetIp);
        console.log(`WS Upgrade: ${hostname} -> ${target}`);

        // upgrade our connection to a websocket connection
        proxy.ws(req, socket, head, { target }, (e) => {
            console.error(`Proxy Error (WS): ${hostname}`, e.message);
            socket.destroy();
        });
    } else {
        socket.destroy();
    }
});

server.listen(80, () => console.log('Proxy Server listening on Port 80'));