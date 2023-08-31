package com.verisure.xad.inv.poc.flink.functions;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.api.common.typeinfo.Types;

import com.verisure.inv.avro.PanelEventAvroDTO;
import com.verisure.inv.avro.PanelEvent;
import com.verisure.inv.avro.SdKey;


public class MyProcessWindowFunction2 extends ProcessWindowFunction< Tuple2<String, String>, String, String, TimeWindow> {

    private transient ValueState<Boolean> flagState;

    @Override
    public void open(Configuration parameters) {
        ValueStateDescriptor<Boolean> flagDescriptor = new ValueStateDescriptor<>(
                "flag",
                Types.BOOLEAN);
        flagState = getRuntimeContext().getState(flagDescriptor);
    }

    @Override
    public void process(String key, Context context, Iterable< Tuple2<String, String> > input, Collector< String > out) throws Exception{
        boolean send = false;
        long count = 0;
        for (Tuple2<String, String> in: input) {
            count++;
            if (!send) {
                //out.collect("Window: " + context.window() + " key: " + key );
                send = true;
            }else{
                //out.collect("FALSE Window: " + context.window() + " key: " + key + " FALSE" );
                send = true;
            }
        }
        /*out.collect("COUNT Window: " + context.window() + " Count: " + count);

        // Get the current state for the current key
        Boolean lastTransactionWasSmall = flagState.value();

        // Check if the flag is set
        if (lastTransactionWasSmall == null) {
            // set the flag to true
            flagState.update(true);
            out.collect("COUNT TRUE Window: " + context.window() + " Count: " + count);
        }
        else{
            out.collect("COUNT FALSE Window: " + context.window() + " Count: " + count);
        }*/
    }

    private void cleanUp(Context ctx) throws Exception {
        flagState.clear();
    }
}