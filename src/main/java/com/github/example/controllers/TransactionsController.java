package com.github.example.controllers;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;


@Controller("/transactions")
public class TransactionsController
{

	@Get
	public HttpStatus getAllTransactions()
	{
		return HttpStatus.OK;
	}
}