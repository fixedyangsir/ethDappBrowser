package com.haoyh.web3viewdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import trust.Call;
import trust.SignMessageRequest;
import trust.SignPersonalMessageRequest;
import trust.SignTransactionRequest;
import trust.SignTypedMessageRequest;
import trust.Trust;
import trust.core.entity.Address;
import trust.core.entity.Message;
import trust.core.entity.Transaction;
import trust.core.entity.TypedData;
import trust.web3.OnSignMessageListener;
import trust.web3.OnSignPersonalMessageListener;
import trust.web3.OnSignTransactionListener;
import trust.web3.OnSignTypedMessageListener;
import trust.web3.Web3View;

public class MainActivity extends AppCompatActivity implements
        OnSignTransactionListener, OnSignPersonalMessageListener, OnSignTypedMessageListener, OnSignMessageListener {

    public static final String TAG = "hyh_web3view-";
    private EditText mEtUrl;
    private Web3View mWeb3View;
    private Button mBtnGo;

    private Call<SignMessageRequest> callSignMessage;
    private Call<SignPersonalMessageRequest> callSignPersonalMessage;
    private Call<SignTypedMessageRequest> callSignTypedMessage;
    private Call<SignTransactionRequest> callSignTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEtUrl = findViewById(R.id.dapp_url);
        mEtUrl.setText("https://js-eth-sign.surge.sh");
        mWeb3View = findViewById(R.id.web3view);
        mBtnGo = findViewById(R.id.go);
        mBtnGo.setOnClickListener(v -> {
            mWeb3View.loadUrl(mEtUrl.getText().toString());
            mWeb3View.requestFocus();
        });

        findViewById(R.id.tv_url_1).setOnClickListener(v -> {
            changeUrlAndOpen((TextView) v);
        });

        findViewById(R.id.tv_url_2).setOnClickListener(v -> {
            changeUrlAndOpen((TextView) v);
        });

        setupWeb3();
    }

    private void changeUrlAndOpen(TextView v) {
        String url = v.getText().toString().trim();
        mEtUrl.setText(url);
        mBtnGo.performClick();
    }

    private void setupWeb3() {
        WebView.setWebContentsDebuggingEnabled(true);
        mWeb3View.setChainId(1);
        mWeb3View.setRpcUrl("https://mainnet.infura.io/v3/bbfb2fa1009e40e4b72b10166d9a5069");
        mWeb3View.setWalletAddress(new Address("0x242776e7ca6271e416e737adffcfeb22e8dc1b3c"));

        mWeb3View.setOnSignMessageListener(message -> {
            Log.d(TAG, "setOnSignMessageListener  " + message.value.toString());
            callSignMessage = Trust.signMessage().message(message).call(this);
        });
        mWeb3View.setOnSignPersonalMessageListener(message -> {
            Log.d(TAG, "setOnSignPersonalMessageListener  " + message.value.toString());
            callSignPersonalMessage = Trust.signPersonalMessage().message(message).call(this);
        });
        mWeb3View.setOnSignTransactionListener(transaction -> {
            Log.d(TAG, "setOnSignTransactionListener  " + transaction.value.toString());
            callSignTransaction = Trust.signTransaction().transaction(transaction).call(this);
        });
        mWeb3View.setOnSignTypedMessageListener(message -> {
            Log.d(TAG, "setOnSignTypedMessageListener  " + message.value.toString());
            callSignTypedMessage = Trust.signTypedMessage().message(message).call(this);
        });
    }

    @Override
    public void onSignMessage(Message<String> message) {
        Toast.makeText(this, message.value, Toast.LENGTH_LONG).show();
        mWeb3View.onSignCancel(message);
    }

    @Override
    public void onSignPersonalMessage(Message<String> message) {
        Toast.makeText(this, message.value, Toast.LENGTH_LONG).show();
        mWeb3View.onSignCancel(message);
    }

    @Override
    public void onSignTypedMessage(Message<TypedData[]> message) {
        Toast.makeText(this, new Gson().toJson(message), Toast.LENGTH_LONG).show();
        mWeb3View.onSignCancel(message);
    }

    @Override
    public void onSignTransaction(Transaction transaction) {
        String str = new StringBuilder()
                .append(transaction.recipient == null ? "" : transaction.recipient.toString()).append(" : ")
                .append(transaction.contract == null ? "" : transaction.contract.toString()).append(" : ")
                .append(transaction.value.toString()).append(" : ")
                .append(transaction.gasPrice.toString()).append(" : ")
                .append(transaction.gasLimit).append(" : ")
                .append(transaction.nonce).append(" : ")
                .append(transaction.payload).append(" : ")
                .toString();
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
        mWeb3View.onSignCancel(transaction);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (callSignTransaction != null) {
            callSignTransaction.onActivityResult(requestCode, resultCode, data, response -> {
                Log.d(TAG, "---000---");
                Transaction transaction = response.request.body();
                if (response.isSuccess()) {
                    mWeb3View.onSignTransactionSuccessful(transaction, response.result);
                } else {
                    if (response.error == Trust.ErrorCode.CANCELED) {
                        mWeb3View.onSignCancel(transaction);
                    } else {
                        mWeb3View.onSignError(transaction, "Some error");
                    }
                }
            });
        }

        if (callSignMessage != null) {
            callSignMessage.onActivityResult(requestCode, resultCode, data, response -> {
                Log.d(TAG, "---111---");
                Message message = response.request.body();
                if (response.isSuccess()) {
                    mWeb3View.onSignMessageSuccessful(message, response.result);
                } else {
                    if (response.error == Trust.ErrorCode.CANCELED) {
                        mWeb3View.onSignCancel(message);
                    } else {
                        mWeb3View.onSignError(message, "Some error");
                    }
                }
            });
        }

        if (callSignPersonalMessage != null) {
            callSignPersonalMessage.onActivityResult(requestCode, resultCode, data, response -> {
                Log.d(TAG, "---222---");
                Message message = response.request.body();
                if (response.isSuccess()) {
                    mWeb3View.onSignMessageSuccessful(message, response.result);
                } else {
                    if (response.error == Trust.ErrorCode.CANCELED) {
                        mWeb3View.onSignCancel(message);
                    } else {
                        mWeb3View.onSignError(message, "Some error");
                    }
                }
            });
        }

        if (callSignTypedMessage != null) {
            callSignTypedMessage.onActivityResult(requestCode, resultCode, data, response -> {
                Log.d(TAG, "---333---");
                Message message = response.request.body();
                if (response.isSuccess()) {
                    mWeb3View.onSignMessageSuccessful(message, response.result);
                } else {
                    if (response.error == Trust.ErrorCode.CANCELED) {
                        mWeb3View.onSignCancel(message);
                    } else {
                        mWeb3View.onSignError(message, "Some error");
                    }
                }
            });
        }
    }

}
