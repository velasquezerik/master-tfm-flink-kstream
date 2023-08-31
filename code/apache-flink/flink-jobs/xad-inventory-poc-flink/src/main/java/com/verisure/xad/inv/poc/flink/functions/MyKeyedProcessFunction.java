package com.verisure.xad.inv.poc.flink.functions;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction.Context;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction.OnTimerContext;
import org.apache.flink.util.Collector;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple;

import com.verisure.inv.avro.PanelEventAvroDTO;
import com.verisure.inv.avro.PanelEvent;
import com.verisure.inv.avro.SdKey;

public class MyKeyedProcessFunction extends KeyedProcessFunction< String, Tuple2<String, String>, String > {

    private static final long serialVersionUID = 1L;

    private static final long ONE_SECOND = 1000;
    private static final long FIVE_SECOND = 5000;
    private static final long ONE_MINUTE = 60 * ONE_SECOND;
    private static final long ONE_HOUR = 60 * ONE_MINUTE;
    private static final long ONE_DAY = 24 * ONE_HOUR;
    private static final long ONE_WEEK = 7 * ONE_DAY;

    private transient ValueState<Boolean> flagState;
    private transient ValueState<Long> timerState;

    @Override
    public void open(Configuration parameters) {
        ValueStateDescriptor<Boolean> flagDescriptor = new ValueStateDescriptor<>(
                "flag",
                Types.BOOLEAN);
        flagState = getRuntimeContext().getState(flagDescriptor);

        ValueStateDescriptor<Long> timerDescriptor = new ValueStateDescriptor<>(
                "timer-state",
                Types.LONG);
        timerState = getRuntimeContext().getState(timerDescriptor);
    }

    @Override
    public void processElement(
            Tuple2<String, String> event,
            Context context,
            Collector<String> out) throws Exception {

        // Get the current state for the current key
        Boolean currentState = flagState.value();

        // Check if the flag is set
        if (currentState == null) {
            // first event
            flagState.update(true);

            //long timer = context.timerService().currentProcessingTime() + ONE_MINUTE;
            long timer = context.timerService().currentProcessingTime() + ONE_DAY;
            context.timerService().registerProcessingTimeTimer(timer);

            out.collect("FIRST event: " + event.f0);

        }else {
            out.collect("OTHER event: " + event);
        }
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) {
        // remove flag after
        timerState.clear();
        flagState.clear();
    }
}