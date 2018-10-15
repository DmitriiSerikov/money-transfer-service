package com.github.example.controller;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;


@Controller("/transactions")
public class TransactionController
{

	@Get
	public HttpStatus getAllTransactions()
	{
		return HttpStatus.OK;
	}
}