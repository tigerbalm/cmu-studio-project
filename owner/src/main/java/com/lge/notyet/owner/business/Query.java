package com.lge.notyet.owner.business;

import com.lge.notyet.owner.ui.MainUI;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;


public class Query {
    public static final String CUSTOM_QUERY= "Custom (Additional developer query)";

    private String displayString;
    protected String[] columnNames;
    protected String sqlQueryString;
    private static ArrayList<GenericQueryHandler> queryList= new ArrayList<GenericQueryHandler>();
    private static GenericQueryHandler customQuery= null;


   public Query(String displayString, String[] columnNames, String sqlQueryString) {
        this.displayString= displayString;
        this.columnNames= columnNames;
        this.sqlQueryString= sqlQueryString;
    }

    static
    {
        queryList.add(new GenericQueryHandler(
                "Peak Usage Times",
                new String[]{"From time of the day", "To time of the day", "Usages"},
                "select @NUM2+1 as 'From time of the day', @NUM2:=t2 as 'To time of the day', truncate(c3, 0) as Usages from (\n" +
                        "select t1 as t2, truncate(@FULLDAYS + (@NUM:= (c2+@NUM)), 0) as c3 from (\n" +
                        "select t1, sum(c1) as c2 from (\n" +
                        "SELECT (hour(from_unixtime(begin_ts))-1) as t1, (-count(*)) as c1  FROM transaction group by hour(from_unixtime(begin_ts)) \n" +
                        "union all\n" +
                        "SELECT hour(from_unixtime(end_ts)), (count(*))  FROM transaction group by hour(from_unixtime(end_ts)) \n" +
                        ") as combinedTable, (SELECT @NUM := 0, @FULLDAYS := sum(datediff(from_unixtime(end_ts), from_unixtime(begin_ts))) from transaction) r\n" +
                        "group by t1\n" +
                        "order by t1 desc\n" +
                        ")as doubleCombined\n" +
                        "union\n" +
                        "select 23, @FULLDAYS\n" +
                        ")as fourthCombined, (select @NUM2 :=-1) as r2\n" +
                        "order by t2\n")
        {
            @Override
            public void fillMoreSettingPanel(JPanel chooseMoreSettingsPanel) {
                super.fillMoreSettingPanel(chooseMoreSettingsPanel);
            }

        });
        //        select @NUM2+1 as 'From time of the day', @NUM2:=t2 as 'To time of the day', truncate(c3, 0) as Usages from (
        //            select t1 as t2, truncate(@FULLDAYS + (@NUM:= (c2+@NUM)), 0) as c3 from (
        //            select t1, sum(c1) as c2 from (
        //                    SELECT (hour(from_unixtime(begin_ts))-1) as t1, (-count(*)) as c1  FROM transaction group by hour(from_unixtime(begin_ts))
        //            union all
        //            SELECT hour(from_unixtime(end_ts)), (count(*))  FROM transaction group by hour(from_unixtime(end_ts))
        //        ) as combinedTable, (SELECT @NUM := 0, @FULLDAYS := sum(datediff(from_unixtime(end_ts), from_unixtime(begin_ts))) from transaction) r
        //        group by t1
        //        order by t1 desc
        //        )as doubleCombined
        //        union
        //        select 23, @FULLDAYS
        //        )as fourthCombined, (select @NUM2 :=-1) as r2
        //        order by t2

        queryList.add(new GenericQueryHandler(
                "Average Occupancy",
                new String[]{"First Day", "Last Day", "Hours Occupied", "Occupancy in hours per day"},
                "select DATE(from_unixtime(min(begin_ts))) as 'First Day:', DATE(from_unixtime(max(end_ts))) as 'Last Day:', sum(end_ts-begin_ts)/3600 as 'Hours Occupied:', (sum(end_ts-begin_ts)/3600)/(1+datediff(from_unixtime(max(end_ts)), from_unixtime(min(begin_ts)))) as 'Occupancy in hours per day' from transaction, reservation, slot, controller where reservation.id = transaction.reservation_id and reservation.slot_id = slot.id and slot.controller_id = controller.id and controller.facility_id in (%facility)")
                //select DATE(from_unixtime(min(begin_ts))) as 'First Day:', DATE(from_unixtime(max(end_ts))) as 'Last Day:', sum(end_ts-begin_ts)/3600 as 'Hours Occupied:', (sum(end_ts-begin_ts)/3600)/(1+datediff(from_unixtime(max(end_ts)), from_unixtime(min(begin_ts)))) as 'Occupancy in hours per day' from transaction, reservation, slot, controller where reservation.id = transaction.reservation_id and reservation.slot_id = slot.id and slot.controller_id = controller.id and controller.facility_id in (1,2,3,4,5)
        {
            @Override
            public void fillMoreSettingPanel(JPanel chooseMoreSettingsPanel) {
            chooseMoreSettingsPanel.removeAll();
            chooseMoreSettingsPanel.setEnabled(false);
            chooseMoreSettingsPanel.revalidate();
        }

        });
        queryList.add(new GenericQueryHandler(
                "How much time cars were parked in each slot",
                new String[]{"slot_id", "Hours parked in the particular slot"},
                "SELECT facility.name as 'Facility Name', concat(slot.controller_id,'-', slot.number) as 'Controller Identification' , round(sum(TIMESTAMPDIFF(SECOND, from_unixtime(begin_ts), from_unixtime(end_ts)))/3600, 1) as 'Hours parked in the particular slot' FROM transaction, reservation, slot, controller, facility where reservation.id=transaction.reservation_id  and reservation.slot_id = slot.id and slot.controller_id = controller.id and controller.facility_id = facility.id and controller.facility_id in (%facility) group by slot.id")
                //SELECT facility.name as 'Facility Name', concat(slot.controller_id,'-', slot.number) as 'Controller Identification' , round(sum(TIMESTAMPDIFF(SECOND, from_unixtime(begin_ts), from_unixtime(end_ts)))/3600, 1) as 'Hours parked in the particular slot' FROM transaction, reservation, slot, controller, facility where reservation.id=transaction.reservation_id  and reservation.slot_id = slot.id and slot.controller_id = controller.id and controller.facility_id = facility.id and controller.facility_id in (1,2,3,4,5) group by slot.id
        {
            @Override
            public void fillMoreSettingPanel(JPanel chooseMoreSettingsPanel) {
                chooseMoreSettingsPanel.removeAll();
                chooseMoreSettingsPanel.setEnabled(false);
                chooseMoreSettingsPanel.revalidate();
            }

        });
        queryList.add(new GenericQueryHandler(
                "Revenue based on facility",
                new String[]{"Revenue in dollars"},
                "SELECT facility.name as 'Facility Name', sum(revenue) 'Revenue in Dollars' FROM transaction, reservation, slot, controller, facility where reservation.id=transaction.reservation_id  and reservation.slot_id = slot.id and slot.controller_id = controller.id and controller.facility_id = facility.id and controller.facility_id in (%facility) group by facility.id")
                //SELECT facility.name as 'Facility Name', sum(revenue) 'Revenue in Dollars' FROM transaction, reservation, slot, controller, facility where reservation.id=transaction.reservation_id  and reservation.slot_id = slot.id and slot.controller_id = controller.id and controller.facility_id = facility.id and controller.facility_id in (1,2,3,4,5) group by facility.id
        {
            @Override
            public void fillMoreSettingPanel(JPanel chooseMoreSettingsPanel) {
                chooseMoreSettingsPanel.removeAll();
                chooseMoreSettingsPanel.setEnabled(false);
                chooseMoreSettingsPanel.revalidate();
        }

        });

        customQuery = new GenericQueryHandler(
                CUSTOM_QUERY,
                new String[]{"It is invalid, need to fetch from db query"},
                "It is invalid now, will be filled by user in text area") {
            JTextArea customQuery = new JTextArea();
            JScrollPane textScrollPane = new JScrollPane(customQuery);

            @Override
            public void fillMoreSettingPanel(JPanel chooseMoreSettingsPanel) {
                chooseMoreSettingsPanel.removeAll();
                chooseMoreSettingsPanel.setLayout(new GridLayout(1,1));
                customQuery.setText(sqlQueryString);
                chooseMoreSettingsPanel.add(textScrollPane);
                chooseMoreSettingsPanel.setEnabled(true);
                chooseMoreSettingsPanel.revalidate();
            }

            @Override
            public String getSqlQuery() {
                sqlQueryString= customQuery.getText();
                return super.getSqlQuery();
            }
        };
    }

    public static GenericQueryHandler getInstance(String queryId, boolean isCustomSQLQuery){
        if(isCustomSQLQuery)
            return customQuery;
        else
            return queryList.get(Integer.parseInt(queryId));

    }

    public static String[] getQueryIdList() {
        String[] returnValue= new String[queryList.size()];
        for (int i=0; i< queryList.size(); i++ ) {
            returnValue[i]= ""+i;
        }
        return returnValue;
    }

    public static String getDefaultQueryId() {
        return "0";
    }

    public String getDisplayString() {
        return displayString;
    }

    public String[] getColumnNames() {
        return columnNames;
    }
    public String getSqlQuery() {
        //return sqlQueryString;
        return sqlQueryString.replace("%facility", MainUI.getFacilityList().toString());
    }

    public void setSQLQuery(String sqlQueryString) {
        this.sqlQueryString= sqlQueryString;
    }
}
