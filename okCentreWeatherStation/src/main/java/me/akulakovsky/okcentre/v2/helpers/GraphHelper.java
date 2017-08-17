package me.akulakovsky.okcentre.v2.helpers;

import android.graphics.Color;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import me.akulakovsky.okcentre.v2.R;


public class GraphHelper {

    public static void setGraphViewSeriesStyle(LineGraphSeries<DataPoint> series, int sensorNumber, int theme){
        series.setThickness(5);
        switch (sensorNumber){
            case 0:
                switch (theme) {
                    case 0:
                        series.setColor(Color.WHITE);
                        break;
                    case 1:
                        series.setColor(Color.BLACK);
                        break;
                }
                break;
            case 1:
                series.setColor(Color.RED);
                break;
            case 2:
                series.setColor(Color.CYAN);
                break;
            case 3:
                series.setColor(Color.GREEN);
                break;
        }
    }

//    public static GraphViewStyle getGraphViewStyle(int theme, int horizontalLabels) {
//        GraphViewStyle graphViewStyle = null;
//        switch (theme) {
//            case 0:
//                graphViewStyle = new GraphViewStyle(Color.WHITE, Color.WHITE, Color.GRAY);
//                break;
//
//            case 1:
//                graphViewStyle = new GraphViewStyle(Color.BLACK, Color.BLACK, Color.GRAY);
//                break;
//        }
//        if (graphViewStyle != null) {
//            graphViewStyle.setGridStyle(GraphViewStyle.GridStyle.VERTICAL);
//            graphViewStyle.setNumHorizontalLabels(horizontalLabels);
//        }
//        return graphViewStyle;
//    }
}
