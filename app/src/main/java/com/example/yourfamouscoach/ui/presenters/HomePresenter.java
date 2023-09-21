package com.example.yourfamouscoach.ui.presenters;

import android.util.Log;

import com.example.yourfamouscoach.domain.usecase.favoritequotes.DeleteSavedQuote;
import com.example.yourfamouscoach.domain.usecase.homescreen.CheckSaved;
import com.example.yourfamouscoach.ui.interfaces.IHomePresenter;
import com.example.yourfamouscoach.ui.interfaces.IHomeView;
import com.example.yourfamouscoach.ui.model.QuotePresentation;

import java.util.List;

import com.example.yourfamouscoach.domain.model.Quote;
import com.example.yourfamouscoach.domain.usecase.homescreen.GetQuotes;
import com.example.yourfamouscoach.domain.usecase.homescreen.SaveQuote;
import com.example.yourfamouscoach.domain.usecase.homescreen.SpecificQuote;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import com.example.yourfamouscoach.utils.QuoteMapper;
import com.squareup.picasso.Picasso;

public class HomePresenter implements IHomePresenter {

    private final IHomeView view;
    private final GetQuotes getQuotesUseCase;
    private final SaveQuote saveQuoteUseCase;
    private final SpecificQuote specificQuoteUseCase;

    private final DeleteSavedQuote deleteSavedQuoteUseCase;

    private final CheckSaved checkSavedUseCase;


    public HomePresenter(IHomeView view, GetQuotes getQuotesUseCase, SaveQuote saveQuoteUseCase, SpecificQuote specificQuote, DeleteSavedQuote deleteSavedQuote, CheckSaved checkSavedUseCase) {
        this.view = view;
        this.getQuotesUseCase = getQuotesUseCase;
        this.saveQuoteUseCase = saveQuoteUseCase;
        this.specificQuoteUseCase = specificQuote;
        this.deleteSavedQuoteUseCase = deleteSavedQuote;
        this.checkSavedUseCase = checkSavedUseCase;
    }

    @Override
    public void fetchData(boolean needsToShowQuote) {
        view.showProgressBar();

        getQuotesUseCase.getQuotes().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Quote>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull List<Quote> quotes) {
                        if (needsToShowQuote) {
                            checkIfIsSaved(quotes.get(0)).subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Boolean>() {
                                        @Override
                                        public void onSubscribe(@NonNull Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(@NonNull Boolean aBoolean) {
                                            view.hideProgressBar();
                                            if (aBoolean) {
                                                view.showFavSaved();
                                                view.checkSavedState(true);

                                            } else {
                                                view.showFavUnsaved();
                                                view.checkSavedState(false);

                                            }

                                        }

                                        @Override
                                        public void onError(@NonNull Throwable e) {

                                        }
                                    });
                            //view.showQuote(quotes.get(0).getQuote(), quotes.get(0).getAuthor());
                            view.showAuthorImage(quotes.get(0).getAuthor(),quotes.get(0).getQuote());
                            //view.adaptText();
                            //view.showBuddha();
                        }
                        //view.hideProgressBar();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        view.hideProgressBar();
                        view.showQuote(e.getMessage(), "");
                    }
                });
    }


    @Override
    public void fetchSpecificQuote(String emotion) {
        view.showProgressBar();
        view.hideQuoteAndAuthorText();
        specificQuoteUseCase.getAnswer(emotion).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Quote>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull Quote quote) {
                        checkIfIsSaved(quote).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new SingleObserver<Boolean>() {
                                    @Override
                                    public void onSubscribe(@NonNull Disposable d) {

                                    }

                                    @Override
                                    public void onSuccess(@NonNull Boolean aBoolean) {
                                        if (aBoolean) {
                                            view.showFavSaved();
                                            view.checkSavedState(true);
                                        } else {
                                            view.showFavUnsaved();
                                            view.checkSavedState(false);
                                        }

                                    }

                                    @Override
                                    public void onError(@NonNull Throwable e) {

                                    }
                                });
                        QuotePresentation quotePresentation = QuoteMapper.mapDomainToPresentation(quote);
                        view.showAuthorImage(quotePresentation.getAuthor(),quotePresentation.getQuote());
//                        view.showQuote(quotePresentation.getQuote(), quotePresentation.getAuthor());
//                        view.adaptText();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
       // view.hideProgressBar();
    }

    private Single<Boolean> checkIfIsSaved(Quote quote) {
        return checkSavedUseCase.checkSaved(quote);
    }

    @Override
    public void onInitView() {
        view.initViews();
    }

    @Override
    public void onFavClicked(boolean saved, String quote, String author, String emotion) {
        Quote quoteToDomain = QuoteMapper.mapPresentationToDomain(new QuotePresentation(quote, author), emotion);
        if (saved) {
            saveQuoteUseCase.saveQuote(quoteToDomain)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onComplete() {
                            view.showFavSaved();
                            view.showMessage("Saved successfully");
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            view.showFavUnsaved();
                            view.showMessage(e.getMessage());
                            Log.i("base", e.getMessage());
                        }
                    });
        } else {
            deleteSavedQuoteUseCase.deleteSavedQuote(QuoteMapper.mapPresentationToDomain(new QuotePresentation(quote, author), emotion))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onComplete() {
                            view.showFavUnsaved();
                            view.showMessage("Unsaved");
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            view.showFavSaved();
                            view.showMessage(e.getMessage());
                            Log.i("base", e.getMessage());
                        }
                    });
        }
    }

    @Override
    public void onShareClicked() {
        //view.launchPermissions();
        view.shareQuote();
    }

    @Override
    public void onNotificationQuote(String quote, String author) {
        view.showBuddha();
        view.showQuote(quote, author);
        view.adaptText();

    }

    @Override
    public void onImageLoad(String author,String quote) {
        view.hideProgressBar();
        view.showQuote(quote, author);
        view.adaptText();
        view.showBuddha();
    }

}
