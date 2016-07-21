/**
 * Wire
 * Copyright (C) 2016 Wire Swiss GmbH
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
package com.waz.zclient.newreg.fragments;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import com.waz.zclient.R;
import com.waz.zclient.controllers.navigation.Page;
import com.waz.zclient.core.stores.appentry.AppEntryError;
import com.waz.zclient.core.stores.appentry.AppEntryState;
import com.waz.zclient.core.stores.appentry.IAppEntryStore;
import com.waz.zclient.newreg.fragments.country.Country;
import com.waz.zclient.newreg.fragments.country.CountryController;
import com.waz.zclient.newreg.utils.AppEntryUtil;
import com.waz.zclient.newreg.views.PhoneConfirmationButton;
import com.waz.zclient.pages.BaseFragment;
import com.waz.zclient.ui.text.TypefaceEditText;
import com.waz.zclient.ui.utils.KeyboardUtils;
import com.waz.zclient.utils.PermissionUtils;
import com.waz.zclient.utils.ViewUtils;

public class EmailAddPhoneFragment extends BaseFragment<EmailAddPhoneFragment.Container> implements TextWatcher,
                                                                                                    CountryController.Observer,
                                                                                                    View.OnClickListener {
    public static final String TAG = EmailAddPhoneFragment.class.getName();

    private static final String[] GET_PHONE_NUMBER_PERMISSIONS = new String[] {Manifest.permission.READ_PHONE_STATE};

    private TextView textViewCountryName;
    private TextView textViewCountryCode;
    private TypefaceEditText editTextPhone;
    private PhoneConfirmationButton phoneConfirmationButton;
    private View buttonCountryChooser;
    private CountryController countryController;
    private TextView textViewNotNow;
    private int phoneNumberStartValidationMinLength;
    private int phoneNumberMinLength;

    private IAppEntryStore.ErrorCallback errorCallback = new IAppEntryStore.ErrorCallback() {
        @Override
        public void onError(AppEntryError appEntryError) {
            if (getContainer() == null) {
                return;
            }
            getContainer().enableProgress(false);

            AppEntryUtil.showErrorDialog(EmailAddPhoneFragment.this.getActivity(),
                                         appEntryError,
                                         new AppEntryUtil.ErrorDialogCallback() {
                                             @Override
                                             public void onOk() {
                                                 KeyboardUtils.showKeyboard(getActivity());
                                             }
                                         });
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_phone, container, false);

        // get references
        textViewCountryName = ViewUtils.getView(view, R.id.ttv_new_reg__signup__phone__country_name);
        textViewCountryCode = ViewUtils.getView(view, R.id.tv__country_code);
        editTextPhone = ViewUtils.getView(view, R.id.et__reg__phone);
        phoneConfirmationButton = ViewUtils.getView(view, R.id.pcb__signup);
        buttonCountryChooser = ViewUtils.getView(view, R.id.ll__signup__country_code__button);
        textViewNotNow = ViewUtils.getView(view, R.id.ttv__not_now);
        ViewUtils.getView(view, R.id.gtv__not_now__close).setVisibility(View.GONE);

        phoneNumberMinLength = getResources().getInteger(R.integer.new_reg__phone_number__min_length);
        phoneNumberStartValidationMinLength = getResources().getInteger(R.integer.new_reg__phone_number__start_validation__min_length);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        countryController = getContainer().getCountryController();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (PermissionUtils.hasSelfPermissions(getActivity(), GET_PHONE_NUMBER_PERMISSIONS)) {
            setSimPhoneNumber();
        }
        editTextPhone.requestFocus();

        onAccentColorHasChanged(getContainer().getAccentColor());

        getControllerFactory().getGlobalLayoutController().setSoftInputModeForPage(Page.PHONE_REGISTRATION);
        KeyboardUtils.showKeyboard(getActivity());

        countryController.addObserver(this);

        Country country = countryController.getCountryFromCode(getStoreFactory().getAppEntryStore().getCountryCode());
        String phone = getStoreFactory().getAppEntryStore().getPhone();

        if (phone != null) {
            editTextPhone.setText(phone);
            editTextPhone.setSelection(phone.length());
            updatePhoneInputControls(phone);
        }
        if (country != null) {
            countryController.setCountry(country);
        }
    }

    private void setSimPhoneNumber() {
        final String abbreviation = getControllerFactory().getDeviceUserController().getPhoneCountryISO();
        final String countryCode = countryController.getCodeForAbbreviation(abbreviation);
        editTextPhone.setText(getControllerFactory().getDeviceUserController().getPhoneNumber(countryCode));
    }

    @Override
    public void onResume() {
        super.onResume();

        buttonCountryChooser.setOnClickListener(this);
        editTextPhone.addTextChangedListener(this);
        phoneConfirmationButton.setOnClickListener(this);
        textViewCountryCode.setOnClickListener(this);
        textViewNotNow.setOnClickListener(this);

        editTextPhone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    confirmPhoneNumber();
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    public void onPause() {
        buttonCountryChooser.setOnClickListener(null);
        editTextPhone.removeTextChangedListener(this);
        phoneConfirmationButton.setOnClickListener(null);
        textViewCountryCode.setOnClickListener(null);
        textViewNotNow.setOnClickListener(null);

        super.onPause();
    }

    @Override
    public void onStop() {
        countryController.removeObserver(this);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        textViewCountryName = null;
        textViewCountryCode = null;
        editTextPhone = null;
        phoneConfirmationButton = null;
        super.onDestroyView();
    }

    private void confirmPhoneNumber() {
        getContainer().enableProgress(true);
        KeyboardUtils.hideKeyboard(getActivity());

        String countryCode = textViewCountryCode.getText().toString();
        String phone = editTextPhone.getText().toString();
        getStoreFactory().getAppEntryStore()
                         .addPhoneToEmail(countryCode,
                                          phone,
                                          errorCallback);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv__country_code:
            case R.id.ll__signup__country_code__button:
                getContainer().openCountryBox();
                break;
            case R.id.pcb__signup:
                confirmPhoneNumber();
                break;
            case R.id.ttv__not_now:
                onNotNow();
                break;
        }
    }

    private void onNotNow() {
        KeyboardUtils.hideKeyboard(getActivity());
        getStoreFactory().getAppEntryStore().setState(AppEntryState.FIRST_LOGIN);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        updatePhoneInputControls(charSequence);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private void updatePhoneInputControls(CharSequence charSequence) {
        phoneConfirmationButton.setState(validatePhoneNumber(charSequence.toString()));

        if (charSequence.length() == 0) {
            editTextPhone.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                      getResources().getDimensionPixelSize(R.dimen.wire__text_size__small));
        } else {
            editTextPhone.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                      getResources().getDimensionPixelSize(R.dimen.wire__text_size__regular));
        }
    }

    private PhoneConfirmationButton.State validatePhoneNumber(String number) {
        if (number.length() > phoneNumberMinLength) {
            return PhoneConfirmationButton.State.CONFIRM;
        } else if (number.length() > phoneNumberStartValidationMinLength) {
            return PhoneConfirmationButton.State.INVALID;
        } else {
            return PhoneConfirmationButton.State.NONE;
        }
    }

    @Override
    public void onCountryHasChanged(Country country) {
        textViewCountryCode.setText(String.format("+%s", country.getCountryCode()));
        textViewCountryName.setText(country.getName());
        updatePhoneInputControls(editTextPhone.getText());
    }

    public void onAccentColorHasChanged(int color) {
        editTextPhone.setAccentColor(color);
        phoneConfirmationButton.setAccentColor(color);
    }

    public static Fragment newInstance() {
        return new EmailAddPhoneFragment();
    }

    public interface Container {
        int getAccentColor();

        void openCountryBox();

        CountryController getCountryController();

        void enableProgress(boolean enabled);
    }
}
