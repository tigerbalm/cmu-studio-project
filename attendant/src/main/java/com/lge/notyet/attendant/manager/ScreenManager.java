package com.lge.notyet.attendant.manager;

import com.lge.notyet.attendant.ui.FacilityMonitorPanel;
import com.lge.notyet.attendant.ui.LoginPanel;
import com.lge.notyet.attendant.ui.Screen;

import javax.swing.*;
import java.awt.*;

public class ScreenManager {

    private JPanel mCards;
    private final CardLayout mMainCardLayout;

    private Screen mLoginPanel;
    private Screen mFacilityMonitorPanel;

    private static ScreenManager sScreenManager = null;

    private ScreenManager () {
        mMainCardLayout = new CardLayout();
    }

    public static ScreenManager getInstance() {
        synchronized (ScreenManager.class) {
            if (sScreenManager == null) {
                sScreenManager = new ScreenManager();
            }
        }
        return sScreenManager;
    }

    public JPanel init() {

        mCards = new JPanel(mMainCardLayout);

        mLoginPanel = new LoginPanel();
        mFacilityMonitorPanel = new FacilityMonitorPanel();

        mCards.add(mLoginPanel.getRootPanel(), mLoginPanel.getName());
        mCards.add(mFacilityMonitorPanel.getRootPanel(), mFacilityMonitorPanel.getName());

        showLoginScreen();
        return mCards;
    }

    public void showLoginScreen() {
        mLoginPanel.initScreen();
        mMainCardLayout.show(mCards, mLoginPanel.getName());
    }

    public void showFacilityMonitorScreen() {
        mFacilityMonitorPanel.initScreen();
        mMainCardLayout.show(mCards, mFacilityMonitorPanel.getName());
    }
}