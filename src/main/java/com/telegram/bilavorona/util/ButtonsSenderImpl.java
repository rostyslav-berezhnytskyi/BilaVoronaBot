package com.telegram.bilavorona.util;

import com.telegram.bilavorona.manager_bot.ManagerBotSender;
import com.telegram.bilavorona.model.FileGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class ButtonsSenderImpl implements ButtonsSender {
    private final MyBotSender botSender;
    private final CommandValidator commandValidator;
    private final ManagerBotSender managerBotSender;

    @Autowired
    public ButtonsSenderImpl(MyBotSender botSender, CommandValidator commandValidator, ManagerBotSender managerBotSender) {
        this.botSender = botSender;
        this.commandValidator = commandValidator;
        this.managerBotSender = managerBotSender;
    }

    @Override
    public void sendPersistentButtons(Long chatId) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        KeyboardButton docsButton = new KeyboardButton("📄 Документація");
        KeyboardButton examplesButton = new KeyboardButton("📋 Приклади виконаних робіт");

        KeyboardButton aiAssistantButton = new KeyboardButton("\uD83E\uDD16 Зв'язатися з AI асистентом");
        KeyboardButton managerButton = new KeyboardButton("\uD83D\uDCE9 Зв'язатися з менеджером");

        KeyboardButton contactsButton = new KeyboardButton("\uD83D\uDCDE Наші контакти");
        KeyboardButton homeButton = new KeyboardButton("\uD83C\uDFE0 Головна");

        KeyboardRow rowOne = new KeyboardRow();
        rowOne.add(docsButton);
        rowOne.add(examplesButton);

        KeyboardRow rowTwo = new KeyboardRow();
        rowTwo.add(aiAssistantButton);
        rowTwo.add(managerButton);

        KeyboardRow rowThree = new KeyboardRow();
        rowThree.add(contactsButton);
        rowThree.add(homeButton);

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(rowOne);
        keyboard.add(rowTwo);
        keyboard.add(rowThree);
        keyboardMarkup.setKeyboard(keyboard);

        botSender.sendKeyboardMarkupMessage(chatId, TextConstants.START_TEXT, keyboardMarkup);
    }

    @Override
    public void sendHomeButtons(Long chatId) {
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton docsButton = new InlineKeyboardButton("📄 Документація");
        docsButton.setCallbackData("documentation");  // Command to be executed

        InlineKeyboardButton examplesButton = new InlineKeyboardButton("📋 Приклади виконаних робіт");
        examplesButton.setCallbackData("examples");  // Command to be executed

        InlineKeyboardButton contactsButton = new InlineKeyboardButton("\uD83D\uDCDE Наші контакти");
        contactsButton.setCallbackData("contacts");  // Command to be executed

        InlineKeyboardButton managerButton = new InlineKeyboardButton("\uD83D\uDCE9 Зв'язатися з менеджером");
        managerButton.setCallbackData("contactManager");  // Command to be executed

        InlineKeyboardButton aiAssistantButton = new InlineKeyboardButton("\uD83E\uDD16 Зв'язатися з AI асистентом");
        aiAssistantButton.setCallbackData("contactAIAssistant");  // Command to be executed

        InlineKeyboardButton discountButton = new InlineKeyboardButton("\uD83D\uDCB8 Отримати знижку");
        discountButton.setCallbackData("get_discount");

        InlineKeyboardButton fqButton = new InlineKeyboardButton("\uD83D\uDEE0\uFE0F Поширенні запитання");
        fqButton.setCallbackData("fq");

        InlineKeyboardButton homeButton = new InlineKeyboardButton("\uD83C\uDFE0 Головна");
        homeButton.setCallbackData("home");

        // Create a row for the buttons
        List<InlineKeyboardButton> rowOne = new ArrayList<>();
        rowOne.add(docsButton);
        rowOne.add(examplesButton);
        rows.add(rowOne);

        List<InlineKeyboardButton> rowTwo = new ArrayList<>();
        rowTwo.add(aiAssistantButton);
        rowTwo.add(managerButton);
        rows.add(rowTwo);

        List<InlineKeyboardButton> rowThree = new ArrayList<>();
        rowThree.add(discountButton);
        rowThree.add(contactsButton);
        rows.add(rowThree);

        List<InlineKeyboardButton> rowFour = new ArrayList<>();
        rowFour.add(fqButton);
        rowFour.add(homeButton);
        rows.add(rowFour);

        inlineKeyboard.setKeyboard(rows);
        botSender.sendInlineKeyboardMarkupMessage(chatId, "\uD83C\uDFE0 Головне меню:", inlineKeyboard);
    }

    @Override
    public void sendRoleSelectionButtons(long chatId, String[] commandParts) {
        log.info("Showing role selection buttons in chatId = {}", chatId);

        if (!commandValidator.checkCom(chatId, commandParts, 2, "Будь ласка вкажіть юзернейм. Приклад: /change_role @username"))
            return;

        String username = commandParts[1];

        // Create buttons for selecting USER or ADMIN roles
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton userButton = new InlineKeyboardButton("USER");
        userButton.setCallbackData("change_role:" + username + ":USER");

        InlineKeyboardButton adminButton = new InlineKeyboardButton("ADMIN");
        adminButton.setCallbackData("change_role:" + username + ":ADMIN");

        InlineKeyboardButton bannedButton = new InlineKeyboardButton("BANNED");
        bannedButton.setCallbackData("change_role:" + username + ":BANNED");

        rows.add(List.of(userButton, adminButton, bannedButton));
        markup.setKeyboard(rows);

        botSender.sendInlineKeyboardMarkupMessage(chatId, "Виберіть нову роль для користувача " + username + ":", markup);
    }

    @Override
    public void sendGroupSelectionButtons(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (FileGroup group : FileGroup.values()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(group.getDisplayName());
            button.setCallbackData("file_group:" + group.name());
            rows.add(Collections.singletonList(button));
        }
        markup.setKeyboard(rows);

        botSender.sendInlineKeyboardMarkupMessage(chatId, "До якої групи ви хочете віднести цей файл?", markup);
    }

    @Override
    public void sendContactUserButton(long userId, long adminId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        InlineKeyboardButton contactButton = new InlineKeyboardButton("📞 Зв'язатися з користувачем");
        contactButton.setUrl("tg://user?id=" + userId);  // Direct link to user's profile

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(Collections.singletonList(contactButton));
        markup.setKeyboard(keyboard);

        // Create and send message with inline button
        managerBotSender.sendInlineKeyboardMarkupMessage(adminId, "Натисніть кнопку нижче, щоб зв'язатися з користувачем:", markup);
    }
}
