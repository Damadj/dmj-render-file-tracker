package render.bots;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import render.controllers.FileTrackerController;

public class FileTrackerBot extends TelegramLongPollingBot {

    private FileTrackerController fileTrackerController;
    private Long id = 0L;
    private String userName = "";

    public FileTrackerBot(FileTrackerController fileTrackerController) {
        this.fileTrackerController = fileTrackerController;
    }

    @Override
    public void onUpdateReceived(Update update) {
        update.getUpdateId();
        SendMessage sendMessage = new SendMessage().setChatId(update.getMessage().getChatId());
        try {
            if (update.getMessage().getText().equals("/start")) {
                sendMessage.setText("Ready to work");
                id = update.getMessage().getChatId();
                userName = System.getProperty("user.name");
                execute(sendMessage);
            }
            if (update.getMessage().getText().equals("/report")) {
                if (fileTrackerController.getTelegramId().getText().equals(update.getMessage().getChatId().toString())) {
                    if (!fileTrackerController.isStarted()) sendMessage.setText("File tracker is not started");
                    else sendMessage.setText(fileTrackerController.buildReport());
                    execute(sendMessage);
                }
            }
            if (update.getMessage().getText().equals("/id")) {
                sendMessage.setText(update.getMessage().getChatId().toString());
                execute(sendMessage);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendMessage(Long chatId, String s) {
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
        return "Dmj_file_tracker_bot";
    }

    @Override
    public String getBotToken() {
        return "1265699567:AAEjarSUkwHgNu2o3Keb7eKev9Ulu3cqCCw";
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
}