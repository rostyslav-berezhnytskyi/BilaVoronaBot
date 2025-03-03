package com.telegram.bilavorona.controler;

import com.telegram.bilavorona.config.MyBotSender;
import com.telegram.bilavorona.model.Role;
import com.telegram.bilavorona.service.UserService;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;


@Slf4j
@Service
public class BotCommandHandlerImpl implements BotCommandHandler {
    private final UserService userService;
    private final MyBotSender botSender;
    private final RoleController roleController;

    private static final String HELP_TEXT = """
                📋 *Команди:*
                
                🔹 /start - Вітальне повідомлення.
                🔹 /help - Довідкова інформація.
                🔹 /contacts - Отримати контактну інформацію
                
                📄 *Робота з файлами:*
                🔹 /documentation - Отримати документи з розділу Документація.
                🔹 /examples - Отримати приклади виконаних робіт.
                
                🔹 /help_admin - Отримати команди для адміністратора.
                """;
    private static final String HELP_TEXT_ADMIN = """
                🛠 *Команди для адміністратора:*
                🔹 /get_all_files - Отримати всі файли.
                🔹 /delete_user - Видаляє користувача за його юзернеймом. Приклад: `/delete_user @username`
                🔹 /change_role - Змінює роль користувача за його юзернеймом. Приклад: `/change_role @username`
                🔹 /send_for_all_user - Відправляє повідомлення/файл всім користувачам. Приклад: `/send_for_all_user після чого потрібно додати файли, або написати повідомлення`
                🔹 /change_file_group_by_id - Змінити групу файлу за його ID. Приклад: `/change_file_group_by_id 123 DOCUMENTATION`
                🔹 /change_file_group_by_name - Змінити групу файлу за його назвою. Приклад: `/change_file_group_by_name file_name.docx EXAMPLES`
                🔹 /change_file_name_by_id - Змінити назву файлу за його ID. Приклад: `/change_file_name_by_id 123 new_name.docx`
                🔹 /change_file_name_by_name - Змінити назву файлу за його поточною назвою. Приклад: `/change_file_name_by_name old_name.docx new_name.docx`
                🔹 /delete_file_by_id - Видалити файл за його ID. Приклад: `/delete_file_by_id 123`
                🔹 /delete_file_by_name - Видалити файл за його назвою. Приклад: `/delete_file_by_name file_name.docx`
                🔹 /get_all_users - Отримати всіх користувачів з БД
                
                📌 *Примітка:* Завантажувати файли в БД мають право тільки *АДМІНІСТРАТОРИ*.
    """;
    private static final String START_TEXT = """
✨ Ласкаво просимо до офіційного бота компанії "БІЛА ВОРОНА"! 🕊️
    📌 Наш продукт — «Велконлак - ГПМ» — це інноваційний двокомпонентний біополімер, створений на 100% з натуральних олій: 🌱 соєвої, рицинової та інших. Завдяки відсутності розчинників та шкідливих полімерів, він є безпечним для контакту з харчовими та медичними продуктами 🥦🧴, що підтверджено санітарно-гігієнічним висновком. ✅
        🔹 Основні властивості «Велконлак - ГПМ»:
•	🌿 Екологічність: Не містить розчинників і полімерів, що можуть розкладатися з виділенням шкідливих сполук.
•	🛡️ Міцність: Стійкий до механічних впливів та хімічних реагентів 🧪, включаючи мийні та дезінфікуючі засоби.
•	🔄 Універсальність: Підходить для покриття підлоги, стін, металоконструкцій, трубопроводів та інших поверхонь.
•	🛠️ Зручність у використанні: Змішується вручну або дрилем 🌀 у співвідношенні від 1,5 до 6:1 за вагою та наноситься шпателем, пензлем або валиком. Полімеризується від 1 до 6 годин. ⏱️
        🌍 Сфери застосування:
•	🏭 Промислові та технічні приміщення: Захист підлоги, стін, покрівлі та обладнання.
•	🥫 Харчова та медична галузь: Безпечний контакт з продуктами харчування, питною водою, спиртами та соками.
•	⚙️ Антикорозійний захист: Металоконструкції, трубопроводи, технічні ємності.
•	🏠 Житлові та комерційні приміщення: Спортивні зали, гаражі, паркінги та складські комплекси.
    📌 Дізнайтеся більше про наші продукти та можливості! 🔍
Натискайте /start або оберіть потрібний розділ у меню. 📲
❓ Якщо у вас виникли запитання — ми завжди раді допомогти! 😊
    """;

    private static final String CONTACTS_TEXT = """
📞 Наші контакти 📞
🏢 Товариство з обмеженою відповідальністю “ВЕЛКОН”

📲 Телефон: +38098 679 41 20
✉️ e-mail: velkon8@gmail.com

📡 Доступні месенджери:

📳 Viber
💬 WhatsApp
✈️ Telegram
🔒 Signal
🛠 Зв'язуйтесь з нами для консультацій та замовлень! 💬
    """;

    @Autowired
    public BotCommandHandlerImpl(UserService userService, MyBotSender botSender, RoleController roleController) {
        this.userService = userService;
        this.botSender = botSender;
        this.roleController = roleController;
    }

    @Override
    public void start(Message msg) {
        String name = msg.getChat().getFirstName();
        log.info("Invoke /start command for user {} in chatId {}", name, msg.getChatId());
        boolean isNewUser = userService.saveUser(msg);
        String greetingForNewUser = "Вітаю , " + name + ", приємно познайомитись! " + EmojiParser.parseToUnicode(":blush:");
        String greetingForOldUser = "Вітаю , " + name + "! " + EmojiParser.parseToUnicode(":wave:");
        String answer = isNewUser ? greetingForNewUser : greetingForOldUser;
        botSender.sendMessage(msg.getChatId(), answer);
        botSender.sendMessage(msg.getChatId(), START_TEXT);
    }

    @Override
    public void help(Message msg) {
        log.info("Invoke /help command. Providing help message for user {} in chatId {}", msg.getChat().getUserName(), msg.getChatId());
        botSender.sendMessage(msg.getChatId(), HELP_TEXT);
    }

    @Override
    public void helpAdmin(Message msg) {
        if (!roleController.checkRole(msg.getChatId(), new Role[]{Role.OWNER, Role.ADMIN})) return;
        log.info("Invoke /helpAdmin command. Providing help message for user {} in chatId {}", msg.getChat().getUserName(), msg.getChatId());
        botSender.sendMessage(msg.getChatId(), HELP_TEXT_ADMIN);
    }

    @Override
    public void contacts(Long chatId) {
        log.info("Invoke contacts command in chatId {}", chatId);
        botSender.sendMessage(chatId, CONTACTS_TEXT);
    }

    @Override
    public void defaultCom(Message msg) {
        log.info("Invoke unknown command. Providing default message for user {} in chatId {}", msg.getChat().getUserName(), msg.getChatId());
        String answer = "Невідома команда. Використовуйте /help, щоб побачити доступні команди.";
        botSender.sendMessage(msg.getChatId(), answer);
    }
}
