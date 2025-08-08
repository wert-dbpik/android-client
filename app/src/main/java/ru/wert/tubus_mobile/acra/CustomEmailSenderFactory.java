package ru.wert.tubus_mobile.acra;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.auto.service.AutoService;

import org.acra.config.CoreConfiguration;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderFactory;

@AutoService(ReportSenderFactory.class)
public class CustomEmailSenderFactory implements ReportSenderFactory {
    @NonNull
    @Override
    public ReportSender create(@NonNull Context context, @NonNull CoreConfiguration config) {
        return new CustomEmailSender(context);
    }

    @Override
    public boolean enabled(@NonNull CoreConfiguration coreConfiguration) {
        return true; // всегда включён
    }
}

