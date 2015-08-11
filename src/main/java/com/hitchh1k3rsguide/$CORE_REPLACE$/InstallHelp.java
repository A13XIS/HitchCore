package com.hitchh1k3rsguide.$CORE_REPLACE$;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class InstallHelp
{

    public static void main(String[] args) throws Exception
    {

        String err = "The mod should be put into the mods folder, and run through minecraft!\n" +
                     "For help check the install section of HitchH1k3r's Mods' webpage.\n" +
                     "http://bit.ly/hitchmods";
        System.out.println(err);

        Object[] options = { "OK", "HELP" };
        int n = JOptionPane
                .showOptionDialog(
                        null,
                        err,
                        "Incorrect Mod Usage", JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE, null, options, options[1]);
        if (n == JOptionPane.NO_OPTION)
        {
            try
            {
                Desktop.getDesktop().browse(new URL("http://hitchh1k3rsguide.com/mods#install").toURI());
            }
            catch (Exception e)
            {
                JOptionPane
                        .showMessageDialog(
                                null,
                                "Could not open browser, please go to http://bit.ly/hitchmods and read the install section.",
                                "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        }

    }

}
