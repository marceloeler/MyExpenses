package org.totschnig.myexpenses.di;

import org.totschnig.myexpenses.MyApplication;
import org.totschnig.myexpenses.activity.ExpenseEdit;
import org.totschnig.myexpenses.activity.MyExpenses;
import org.totschnig.myexpenses.dialog.TransactionDetailFragment;
import org.totschnig.myexpenses.dialog.TransactionListDialogFragment;
import org.totschnig.myexpenses.export.pdf.PdfPrinter;
import org.totschnig.myexpenses.fragment.CategoryList;
import org.totschnig.myexpenses.fragment.SplitPartList;
import org.totschnig.myexpenses.fragment.StaleImagesList;
import org.totschnig.myexpenses.fragment.TemplatesList;
import org.totschnig.myexpenses.fragment.TransactionList;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, UiModule.class, UtilsModule.class})
public interface AppComponent {
  void inject(MyApplication application);

  void inject(ExpenseEdit expenseEdit);

  void inject(MyExpenses myExpenses);

  void inject(TransactionDetailFragment transactionDetailFragment);

  void inject(StaleImagesList staleImagesList);

  void inject(PdfPrinter pdfPrinter);

  void inject(TemplatesList templatesList);

  void inject(TransactionList transactionList);

  void inject(SplitPartList splitPartList);

  void inject(TransactionListDialogFragment transactionListDialogFragment);

  void inject(CategoryList categoryList);
}
