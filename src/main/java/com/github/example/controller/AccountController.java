package com.github.example.controller;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;


@Controller("/accounts")
public class AccountController
{

	@Get
	public HttpStatus getAllAccounts()
	{
		return HttpStatus.OK;
	}
}