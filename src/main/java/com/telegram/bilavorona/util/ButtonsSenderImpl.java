package com.telegram.bilavorona.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ButtonsSenderImpl implements ButtonsSender{
    private final MyBotSender botSender;

    @Autowired
    public ButtonsSenderImpl(MyBotSender botSender) {
        this.botSender = botSender;
    }

    @Override
    public void sendPersistentButtons(Long chatId) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        KeyboardButton docsButton = new KeyboardButton("📄 Документація");
        KeyboardButton examplesButton = new KeyboardButton("📋 Приклади виконаних робіт");
        KeyboardButton contactsButton = new KeyboardButton("\uD83D\uDCDE Наші контакти");

        // Creating rows for buttons
        KeyboardRow row = new KeyboardRow();
        row.add(docsButton);
        row.add(examplesButton);
        row.add(contactsButton);

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);

        botSender.sendKeyboardMarkupMessage(chatId,TextConstants.START_TEXT, keyboardMarkup);
    }

    @Override
    public void sendInlinePersistentButtons(Long chatId) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton docsButton = new InlineKeyboardButton("📄 Документація");
        docsButton.setCallbackData("/documentation");  // Command to be executed

        InlineKeyboardButton examplesButton = new InlineKeyboardButton("📋 Приклади виконаних робіт");
        examplesButton.setCallbackData("/examples");  // Command to be executed

        InlineKeyboardButton contactsButton = new InlineKeyboardButton("\uD83D\uDCDE Наші контакти");
        contactsButton.setCallbackData("/contacts");  // Command to be executed

        // Create a row for the buttons
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(docsButton);
        row.add(examplesButton);
        row.add(contactsButton);
        rows.add(row);

        inlineKeyboard.setKeyboard(rows);
        botSender.sendInlineKeyboardMarkupMessage(chatId, "⬇️ Виберіть одну з базових дій:", inlineKeyboard);
    }

    @Override
    public void sendRoleSelectionButtons(long chatId, String[] commandParts) {
        log.info("Showing role selection buttons in chatId = {}", chatId);

        if (commandParts.length <= 1) {
            botSender.sendMessage(chatId, "Будь ласка вкажіть юзернейм. Приклад: /change_role @username");
            return;
        }
        String username = commandParts[1];

        // Create buttons for selecting USER or ADMIN roles
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton userButton = new InlineKeyboardButton("USER");
        userButton.setCallbackData("change_role:" + username + ":USER");

        InlineKeyboardButton adminButton = new InlineKeyboardButton("ADMIN");
        adminButton.setCallbackData("change_role:" + username + ":ADMIN");

        rows.add(List.of(userButton, adminButton));
        markup.setKeyboard(rows);

        botSender.sendInlineKeyboardMarkupMessage(chatId, "Виберіть нову роль для користувача " + username + ":", markup);
    }
}
