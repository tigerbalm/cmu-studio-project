package com.lge.notyet.driver.ui;

import javax.swing.*;

/**
 * Created by beney.kim on 2016-06-14.
 */
public class ReservationHistoryPanel {
    private JLabel mLabelUserName;
    private JLabel mLabalReservationDate;
    private JLabel mLabalReservationLocation;
    private JPanel mForm;

    public JPanel getRootPanel() {
        return mForm;
    }

    public String getName() {
        return "ReservationHistoryPanel";
    }
}
