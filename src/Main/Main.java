package Main;


import org.dreambot.api.methods.Calculations;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import javax.swing.*;
import java.awt.*;

@ScriptManifest(category = Category.MISC, name = "Mixer", author = "vpk", version = 1.0, description = "Testing")
public class Main extends AbstractScript {


    private boolean isRunning = false;
    private boolean isAntiBanChecked = false;
    private String firstMix;
    private String secondMix;
    private String itemsToMix;
    private Timer timer = new Timer();
    private ZenAntiBan antiban;
    private int countItems;

    private boolean making;
    private String status = "";

    public enum State {
        NOTHING, BANK, MAKE,
    }


    private State getState() {

        if (getInventory().contains(firstMix) && getInventory().contains(secondMix)) {
                return State.MAKE;
            } else {
                making = false;
                return State.BANK;
            }

    }




    @Override
    public void onStart() {
        // Initialize anti-ban instance
        antiban = new ZenAntiBan(this);

        createGUI();
    }




    @Override
    public int onLoop() {



    if(isRunning) {
        switch (getState()) {


            case NOTHING:
                break;
            case BANK:
                status = "Banking";
                GameObject bank = getGameObjects().closest(gameObject -> gameObject != null && gameObject.hasAction("Bank"));
                if (bank != null && bank.interact("Bank")) {
                    if (sleepUntil(() -> getBank().isOpen(), 9000)) {
                        if (getBank().depositAllItems()) {
                            if (sleepUntil(() -> !getInventory().isFull(), 8000)) {
                                if (getBank().withdraw(firstMix, Integer.parseInt(itemsToMix))) {
                                    sleep(300 + Calculations.random(300));
                                    sleepUntil(() -> !getInventory().contains(firstMix), 1000 + Calculations.random(2000));
                                    if (getBank().withdraw(secondMix, Integer.parseInt(itemsToMix))) {
                                        sleep(300 + Calculations.random(300));
                                        sleepUntil(() -> !getInventory().contains(secondMix), 1000 + Calculations.random(2000));
                                        if (getBank().close()) {
                                            sleepUntil(() -> !getBank().isOpen(), 8000);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            case MAKE:
                status = "Making";
                WidgetChild makeAll = getWidgets().getWidgetChild(270, 14);
                WidgetChild pizzaBase = getWidgets().getWidgetChild(270, 16);


                if(makeAll != null && makeAll.isVisible()) {
                    sleep(Calculations.random(300, 600));
                    getDialogues().typeOption(1);
                    sleepUntil(() -> !getInventory().contains(firstMix), 60000);
                }
                else if (pizzaBase != null && pizzaBase.isVisible()) {
                    sleep(Calculations.random(900, 1200));
                    getDialogues().typeOption(3);
                    sleepUntil(() -> !getInventory().contains(firstMix), 60000);
                }
                else
                break;{
                getInventory().get(firstMix).useOn(secondMix);
                sleepUntil(() -> makeAll != null, 1500 + Calculations.random(500));
            }


        }


        // Check for random flag (for adding extra customized anti-ban features)
        if (antiban.doRandom())
            log("Script-specific random flag triggered");



    }

        // Call anti-ban (returns a wait time after performing any actions)
        return antiban.antiBan();
    }

    @Override
    public void onPaint(Graphics g) {
        g.setColor(Color.GREEN);
        g.drawString("Runtime: " + timer.formatTime(), 25, 40);
        g.drawString("State: " + status, 10, 60);
        g.drawString("Items made: " + countItems, 10, 80);
        g.drawString("Anti-Ban Status: " + (antiban.getStatus().equals("") ? "Inactive" : antiban.getStatus()), 10, 100);
    }


    private void createGUI(){

        JFrame frame = new JFrame();
        frame.setTitle("vpk mixer");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(getClient().getInstance().getCanvas());
        frame.setPreferredSize(new Dimension(300, 180));
        frame.getContentPane().setLayout(new BorderLayout());



        JPanel settingPanel = new JPanel();
        settingPanel.setLayout(new GridLayout(0,2));

        JLabel firstItem = new JLabel();
        firstItem.setText("First item to mix");
        settingPanel.add(firstItem);

        JTextField firstItemTextField = new JTextField();
        settingPanel.add(firstItemTextField);

        JLabel secondItem = new JLabel();
        secondItem.setText("Second item");
        settingPanel.add(secondItem);

        JTextField secondItemTextField = new JTextField();
        settingPanel.add(secondItemTextField);


        JLabel itemsToWithdraw = new JLabel();
        itemsToWithdraw.setText("Items to withdraw");
        settingPanel.add(itemsToWithdraw);

        JTextField itemsToWithdrawTextField = new JTextField("14");
        settingPanel.add(itemsToWithdrawTextField);



        frame.getContentPane().add(settingPanel, BorderLayout.CENTER);

        JCheckBox antibanCheckBox = new JCheckBox();
        antibanCheckBox.setText("Anti-Ban");
        settingPanel.add(antibanCheckBox);



        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());


        JButton button = new JButton();
        button.setText("Start");
        button.addActionListener(l -> {
            firstMix = firstItemTextField.getText();
            secondMix = secondItemTextField.getText();
            itemsToMix = itemsToWithdrawTextField.getText();


            isRunning = true;
            frame.dispose();
        });
        buttonPanel.add(button);

        button = new JButton();
        button.setText("Close");
        button.addActionListener(l -> {

            isRunning = false;
            frame.dispose();
        });
        buttonPanel.add(button);



        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }







}
