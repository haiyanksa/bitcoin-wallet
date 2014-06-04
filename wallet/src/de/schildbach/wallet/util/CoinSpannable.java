/*
 * Copyright 2014 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.schildbach.wallet.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import com.google.bitcoin.core.Coin;
import com.google.bitcoin.utils.CoinFormat;

import de.schildbach.wallet.Constants;

/**
 * @author Andreas Schildbach
 */
public final class CoinSpannable extends SpannableString
{
	public CoinSpannable(final CoinFormat format, final boolean signed, @Nullable final Coin coin)
	{
		super(format(format, signed, coin));
	}

	public CoinSpannable(final CoinFormat format, @Nullable final Coin coin)
	{
		super(format(format, false, coin));
	}

	private static CharSequence format(final CoinFormat format, final boolean signed, final Coin coin)
	{
		if (coin == null)
			return "";

		checkArgument(coin.signum() >= 0 || signed);

		if (signed)
			return format.negativeSign(Constants.CURRENCY_MINUS_SIGN).positiveSign(Constants.CURRENCY_PLUS_SIGN).format(coin);
		else
			return format.format(coin);
	}

	public CoinSpannable applyMarkup(@Nullable final Object prefixSpan1, @Nullable final Object prefixSpan2, @Nullable final Object insignificantSpan)
	{
		applyMarkup(this, prefixSpan1, prefixSpan2, BOLD_SPAN, insignificantSpan);
		return this;
	}

	public static final Object BOLD_SPAN = new StyleSpan(Typeface.BOLD);
	public static final RelativeSizeSpan SMALLER_SPAN = new RelativeSizeSpan(0.85f);

	private static final Pattern P_PARTS = Pattern.compile("(\\w+\\s)?" + "([" + Constants.CURRENCY_PLUS_SIGN + Constants.CURRENCY_MINUS_SIGN
			+ "]?\\d*(?:\\.\\d{0,2})?)" + "(\\d+)?");

	public static void applyMarkup(@Nonnull final Spannable spannable, @Nullable final Object prefixSpan1, @Nullable final Object prefixSpan2,
			@Nullable final Object significantSpan, @Nullable final Object insignificantSpan)
	{
		if (prefixSpan1 != null)
			spannable.removeSpan(prefixSpan1);
		if (prefixSpan2 != null)
			spannable.removeSpan(prefixSpan2);
		if (significantSpan != null)
			spannable.removeSpan(significantSpan);
		if (insignificantSpan != null)
			spannable.removeSpan(insignificantSpan);

		final Matcher m = P_PARTS.matcher(spannable);
		if (m.find())
		{
			int i = 0;

			if (m.group(1) != null) // currency code
			{
				if (prefixSpan1 != null)
					spannable.setSpan(prefixSpan1, i, m.end(1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				if (prefixSpan2 != null)
					spannable.setSpan(prefixSpan2, i, m.end(1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				i = m.end(1);
			}

			if (m.group(2) != null) // significant part of amount
			{
				if (significantSpan != null)
					spannable.setSpan(significantSpan, i, m.end(2), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				i = m.end(2);
			}

			if (m.group(3) != null) // insignificant part of amount
			{
				if (insignificantSpan != null)
					spannable.setSpan(insignificantSpan, i, m.end(3), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				i = m.end(3);
			}

			// if (prefixColorSpan != null)
			// spannable.setSpan(prefixColorSpan, 0, pivot, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}
}
