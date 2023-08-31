package com.verisure.xad.inv.poc.flink.functions;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.api.java.tuple.Tuple2;

import com.verisure.inv.avro.PanelEventAvroDTO;
import com.verisure.inv.avro.PanelEvent;
import com.verisure.inv.avro.SdKey;


public class MyProcessWindowFunction extends ProcessWindowFunction< Tuple2<String, String>, Tuple2<String, String>, String, TimeWindow> {

    private int threshold;

    public MyProcessWindowFunction(int threshold) {
    	this.threshold = threshold;
    }

    @Override
    public void process(String key, Context context, Iterable< Tuple2<String, String> > input, Collector< Tuple2<String, String> > out) {
        long count = 0;
        String object = new String();
        for (Tuple2<String, String> in: input) {
            count++;
            object = in.f1;
        }
        if (count >= threshold) {
            out.collect(new Tuple2<String, String>(key, object));
        }
    }
}