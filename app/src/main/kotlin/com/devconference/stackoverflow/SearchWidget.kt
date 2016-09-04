package com.devconference.stackoverflow

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.widget.EditText
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class SearchWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : EditText(context, attrs, defStyleAttr, defStyleRes) {

  companion object {
    val TAG: String = SearchWidget::class.java.simpleName
  }

  private fun textChangeObservable(): Observable<CharSequence> {
    return Observable.create { subscriber -> addTextChangedListener(createSearchTextWatcher(subscriber)) }
  }

  private fun createSearchTextWatcher(subscriber: Subscriber<in CharSequence>) = object : TextWatcher {
    override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
      if (!subscriber.isUnsubscribed) {
        subscriber.onNext(s)
      }
    }

    override fun afterTextChanged(p0: Editable?) {
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }
  }

  fun textChangeSearchBehaviorObservable(): Observable<String>
      = textChangeObservable()
      .skip(3)
      .doOnNext { charSequence -> Log.v(TAG, "Buscando: " + charSequence) }
      .throttleLast(100, TimeUnit.MILLISECONDS)
      .debounce(200, TimeUnit.MILLISECONDS)
      .onBackpressureLatest()
      .observeOn(AndroidSchedulers.mainThread())
      .filter { charSequence -> !charSequence.isNullOrBlank() }
      .map { charSequence -> charSequence.toString() }

}