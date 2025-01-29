package net.minecraft.client.main;

import tech.skidonion.obfuscator.annotations.StringEncryption;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

@StringEncryption
public class Progress extends JFrame {
//      
//      private final JProgressBar progressBar;
      public static boolean done = false;

      public Progress() {
            this.setSize(512, 300);
            this.setAlwaysOnTop(true);
            this.setLayout(new BorderLayout());
            this.setLocationRelativeTo(null);
            this.setUndecorated(true);
            final ImageIcon backgroundImage = new ImageIcon(Objects.requireNonNull(this.getClass().getResource("/assets/minecraft/client/astolfos/furry2.gif")));
            final JLabel backgroundLabel = new JLabel(backgroundImage);
            this.add(backgroundLabel, "Center");
            this.setVisible(true);
            for (int i = 0; i <= 100; i++) {
                  try {
                        Thread.sleep(30);
                  } catch (InterruptedException e) {
                        e.printStackTrace();
                  }

            }
            this.setVisible(false);
      }
}
