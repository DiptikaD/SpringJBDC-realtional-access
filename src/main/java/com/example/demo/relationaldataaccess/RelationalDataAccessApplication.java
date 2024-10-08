package com.example.demo.relationaldataaccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class RelationalDataAccessApplication implements CommandLineRunner {

	private static final Logger log =
			LoggerFactory.getLogger(RelationalDataAccessApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(RelationalDataAccessApplication.class, args);
	}

	@Autowired
	JdbcTemplate jbdcTemplate;

	@Override
	public void run(String... args) throws Exception {

		log.info("Creating tables!!");

		jbdcTemplate.execute("DROP TABLE customers IF EXISTS");
		jbdcTemplate.execute("CREATE TABLE customers(" +
				"id SERIAL, first_name VARCHAR(255), last_name VARCHAR(255))");

		//split up the array of whole names into an array of first/last name

		List<Object[]> splitUpNames = Arrays.asList("John WooHoo", "Jeff Bean", "Josh Glock", "Josh Glong").stream()
				.map(name -> name.split(" "))
				.collect(Collectors.toList());

		//Use a java8 stream to printout each tuple of the list
		//tuple = In mathematics, a tuple is an ordered sequence of values.
		splitUpNames.forEach(name ->
				log.info(String.format("Inserting customer record for %s %s", name[0], name[1])));

		//uses JdbcTemplate's batchUpdate operation to bulk load data
		jbdcTemplate.batchUpdate("INSERT INTO customers(first_name, last_name) VALUES (?,?)", splitUpNames);

		log.info("Querying for customer records where first_name = 'josh':");
		jbdcTemplate.query(
				"SELECT id, first_name, last_name FROM customers WHERE first_name = ?",
				(rs, rowNum) -> new Customer(rs.getLong("id"),
						rs.getString("first_name"), rs.getString("last_name")), "Josh")
				.forEach(customer -> log.info(customer.toString()));
	}
}
