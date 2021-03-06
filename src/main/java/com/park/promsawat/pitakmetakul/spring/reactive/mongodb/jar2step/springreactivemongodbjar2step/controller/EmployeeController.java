package com.park.promsawat.pitakmetakul.spring.reactive.mongodb.jar2step.springreactivemongodbjar2step.controller;


import com.park.promsawat.pitakmetakul.spring.reactive.mongodb.jar2step.springreactivemongodbjar2step.model.Employee;
import com.park.promsawat.pitakmetakul.spring.reactive.mongodb.jar2step.springreactivemongodbjar2step.model.EmployeeEvent;
import com.park.promsawat.pitakmetakul.spring.reactive.mongodb.jar2step.springreactivemongodbjar2step.repository.EmployeeRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Date;
import java.util.stream.Stream;

@RestController
@RequestMapping("/employee")
public class EmployeeController {

    private EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/all")
    public Flux<Employee> getAll() {
        return employeeRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Employee> getById(@PathVariable("id") String id) {
        return employeeRepository.findById(id);
    }

    //produces = .... Using for steam data gradually all time not just one object
    @GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EmployeeEvent> getEvents(@PathVariable("id") final String id) {
        return employeeRepository.findById(id)
                .flatMapMany(employee -> {

                    Flux<Long> interval = Flux.interval(Duration.ofSeconds(2));

                    Flux<EmployeeEvent> employeeEventFlux =
                            Flux.fromStream(
                                    Stream.generate(() -> new EmployeeEvent(employee,
                                            new Date()))
                            );

                    return Flux.zip(interval, employeeEventFlux)
                            .map(Tuple2::getT2);
                });
    }

    // return json by define second by user
    @GetMapping(value = "/{id}/events/{sec}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EmployeeEvent> getEventSec(@PathVariable("id") final String id, @PathVariable("sec") final int sec) {
        return employeeRepository.findById(id)
                .flatMapMany(employee -> {
                    Flux<Long> interval = Flux.interval(Duration.ofSeconds(sec));
                    Flux<EmployeeEvent> employeeEventFlux =
                            Flux.fromStream(Stream.generate(() -> new EmployeeEvent(employee, new Date())));
                    return Flux.zip(interval, employeeEventFlux)
                            .map(Tuple2::getT2);
                });
    }
}
