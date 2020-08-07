package render.bots;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class FileTrackerBot extends TelegramLongPollingBot {

    private String id = "";

    @Override
    public void onUpdateReceived(Update update) {
        update.getUpdateId();
        SendMessage sendMessage = new SendMessage().setChatId(update.getMessage().getChatId());
        if (update.getMessage().getText().equals("/start")) {
            sendMessage.setText("Ready to work");
            id = update.getMessage().getChatId().toString();
            try {
                execute(sendMessage);
            } catch (TelegramApiException e){
                e.printStackTrace();
            }
        }
    }

    public synchronized void sendMessage(String chatId, String s) {
        SendMessage message = new SendMessage();

        message.setChatId(chatId);
        message.setText(s);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        // Return bot username
        // If bot username is @MyAmazingBot, it must return 'MyAmazingBot'
        return "Dmj_file_tracker_bot";
    }

    @Override
    public String getBotToken() {
        // Return bot token from BotFather
        return "1265699567:AAEjarSUkwHgNu2o3Keb7eKev9Ulu3cqCCw";
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
}