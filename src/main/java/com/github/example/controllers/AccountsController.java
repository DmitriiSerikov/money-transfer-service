package com.github.example.controllers;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;


@Controller("/accounts")
public class AccountsController
{

	@Get
	public HttpStatus getAllAccounts()
	{
		return HttpStatus.OK;
	}
}