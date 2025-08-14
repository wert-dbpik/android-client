package ru.wert.tubus_mobile.acra;

import android.app.Application;

import org.acra.ACRA;
import org.acra.BuildConfig;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.DialogConfigurationBuilder;
import org.acra.config.HttpSenderConfigurationBuilder;
import org.acra.data.StringFormat;
import org.acra.sender.HttpSender;

import ru.wert.tubus_mobile.R;

public class ACRA_config {

    public static void create(Application application) {

        String serverUrl = "http://192.168.2.132:8080/crash-reports/create";

        // Основная конфигурация
        CoreConfigurationBuilder builder = new CoreConfigurationBuilder()
                .withBuildConfigClass(BuildConfig.class)
                .withReportFormat(StringFormat.JSON)
                .withDeleteUnapprovedReportsOnApplicationStart(false)
                .withSendReportsInDevMode(true)
                .withPluginConfigurations(
                        new DialogConfigurationBuilder()
                                .withResTheme(R.style.AppTheme)
                                .withTitle("Внимание")
                                .withText("Произошел досадный сбой! " +
                                        "Отчет об ошибке будет автоматически отправлен разработчику.")
                                .withPositiveButtonText("ОК")
                                .withEnabled(true)
                                .build(),
                        new HttpSenderConfigurationBuilder()
                                .withUri(serverUrl)
                                .withHttpMethod(HttpSender.Method.POST)
                                .withEnabled(true)
                                .withConnectionTimeout(30_000)
                                .withSocketTimeout(30_000)
                                .build()
                );

        // Инициализируем ACRA
        ACRA.init(application, builder);

    }
}
