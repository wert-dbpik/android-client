package ru.wert.tubus_mobile;

import android.app.Application;

import org.acra.ACRA;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.DialogConfigurationBuilder;
import org.acra.config.MailSenderConfigurationBuilder;
import org.acra.data.StringFormat;

/**
 * ACRA предоставляет возможность перехватывать сбой, произошедший в программе во время выполнения
 * и отправлять разработчику письмо на почту с логами и описанием произошедшей ошибки
 */
public class ACRA_config {

    public static void create(Application application) {

        String user = ThisApplication.getProp("USER_NAME");

        ACRA.init(application, new CoreConfigurationBuilder()
                //core configuration:
                .withBuildConfigClass(BuildConfig.class)
                // Отключаем повторную отправку неотправленных отчетов при старте приложения
                .withDeleteUnapprovedReportsOnApplicationStart(false)
                .withReportFormat(StringFormat.JSON)
                .withPluginConfigurations(
                        new DialogConfigurationBuilder()
                                //optional, enables the dialog title
                                .withTitle("Внимание!")
                                //required
                                .withText("В программе произошел досадный сбой, " +
                                        "для скорейшего исправления произошедшей ошибки " +
                                        "необходимо ОТПРАВИТЬ ОТЧЕТ разработчику.")
                                //defaults to android.R.string.ok
                                .withPositiveButtonText("Отправить отчет")
                                //defaults to android.R.string.cancel
                                .withNegativeButtonText("Нет")
                                .withResTheme(R.style.AppTheme)
                                .withEnabled(true)// Без этой строки не отправляет почту
                                .build(),
                        new MailSenderConfigurationBuilder()
                                //required
                                .withMailTo("wert001@yandex.ru")
                                //defaults to true
                                .withReportAsFile(true)
                                //defaults to ACRA-report.stacktrace
                                .withReportFileName("TubusMobile_Crash.txt")
                                //defaults to "<applicationId> Crash Report"
                                .withSubject("TubusMobile-android : сбой в программе")
                                //defaults to empty
                                .withBody(String.format("%s сообщает о сбое.\nОтчет во вложенном файле " +
                                        "TubusM_Crash.txt", user))
                                .build()
                )

        );
    }
}
