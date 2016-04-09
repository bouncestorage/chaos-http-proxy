package com.bouncestorage.chaoshttpproxy;

import java.util.List;
import java.util.Random;

import com.google.common.base.Supplier;

public class RandomFailureSupplier implements Supplier<Failure>{
	
    private final Random random = new Random();
    private final List<Failure> failures;
    
    public RandomFailureSupplier(List<Failure> failures){
    	this.failures = failures;
    }

    @Override
    public Failure get() {
        return failures.get(random.nextInt(failures.size()));
    }
}
