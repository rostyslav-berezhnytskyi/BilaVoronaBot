package com.telegram.bilavorona.bila_vorona_manager;

import com.telegram.bilavorona.util.RoleValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@Component
public class ManagerCommandHandlerImpl implements ManagerCommandHandler {
    private final ManagerBotSender managerBotSender;
    private final RoleValidator roleValidator;

    @Autowired
    public ManagerCommandHandlerImpl(ManagerBotSender managerBotSender, RoleValidator roleValidator) {
        this.managerBotSender = managerBotSender;
        this.roleValidator = roleValidator;
    }


    public void start(Message msg) {
        Long chatId = msg.getChatId();
        log.info("Invoke /start command in BilaVoronaManagerBot in chatId {}", chatId);

        if(roleValidator.checkRoleOwnerOrAdmin(chatId)) {
            managerBotSender.sendMessage(chatId,
                    """
                            👋 Вітаємо в боті менеджера BilaVorona Manager Bot!
                             Цей бот дозволяє вам отримувати повідомлення та запити від користувачів основного бота @bila_vorona_bot та відповідати на них. 📩 \s
    
                             🛠️ Доступні команди:
                             - /help — довідка щодо роботи з ботом
                             - /reply — відповісти користувачу
    
                             Якщо виникнуть питання, звертайтеся до адміністратора.
                             Бажаємо продуктивної роботи! 🚀""");
        } else {
            managerBotSender.sendMessage(chatId,
                    """
                            ❌ Вибачте, але ви не маєте доступу до цього бота.
                            Цей бот призначений лише для менеджерів компанії BilaVorona.
                            Якщо ви шукаєте інформацію або хочете поставити запитання, будь ласка, скористайтеся основним ботом — @bila_vorona_bot
                            Дякуємо за розуміння! 💙 """);
        }
    }

    public void help(long chatId) {
        log.info("Invoke /help command in BilaVoronaManagerBot in chatId {}", chatId);
        managerBotSender.sendMessage(chatId, """
                🛠️ Доступні команди:
                 - /help — довідка щодо роботи з ботом
                 - /reply — відповісти користувачу""");
    }

    public void defaultCom(long chatId) {
        log.info("Invoke unknown command in BilaVoronaManagerBot. Providing default message in chatId {}", chatId);
        String answer = "Невідома команда. Використовуйте /help, щоб побачити доступні команди.";
        managerBotSender.sendMessage(chatId, answer);
    }
}
