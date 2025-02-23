package dev.faiths.module.misc;

import dev.faiths.Faiths;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.utils.ClientUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ModuleGetYourPhone extends CheatModule {

    public ModuleGetYourPhone() {
        super("GetYourPhone", Category.Misc , "获取你的手机号");
    }

    @Override
    public void onEnable() {
        String filePath = "C:\\Program Files\\ToDesk\\config.ini";
        String filePath2 = "D:\\Todesk\\config.ini";
        String filePath3 = "C:\\Program Files (x86)\\ToDesk\\config.ini";
        String targetKey = "LoginPhone=";

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(targetKey)) {
                    String value = line.substring(targetKey.length());
                    ClientUtils.displayChatMessage("你的手机号是不是 " + value);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            ClientUtils.displayChatMessage("java.io.FileNotFoundException");
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath2))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(targetKey)) {
                    String value = line.substring(targetKey.length());
                    ClientUtils.displayChatMessage("你的手机号是不是 " + value);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            ClientUtils.displayChatMessage("java.io.FileNotFoundException");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath3))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(targetKey)) {
                    String value = line.substring(targetKey.length());
                    ClientUtils.displayChatMessage("你的手机号是不是 " + value);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            ClientUtils.displayChatMessage("java.io.FileNotFoundException");
        }

        Faiths.moduleManager.getModule(ModuleGetYourPhone.class).setState(false);
    }
}
