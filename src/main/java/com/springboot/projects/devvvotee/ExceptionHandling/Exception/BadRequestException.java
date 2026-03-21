package com.springboot.projects.devvvotee.ExceptionHandling.Exception;

public class BadRequestException extends RuntimeException
{
    public BadRequestException(String message)
    {
        super(message);
    }
}
