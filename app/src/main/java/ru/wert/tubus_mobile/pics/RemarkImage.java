package ru.wert.tubus_mobile.pics;

import android.net.Uri;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.wert.tubus_mobile.data.models.Pic;

/**
 * Класс описывает состояние изображения в ресайклере,
 * необходим, чтобы можно было оперировать как сохраненными, так и еще не сохраненными изображениями
 * -------------
 * Поле uri = null, если изображение еще не загружено в кэш
 * Поле pic = null, если изображение еще не сохранено в БД
 */
@Getter
@Setter
@AllArgsConstructor
public class RemarkImage {
    Uri uri;
    Pic pic;
}
