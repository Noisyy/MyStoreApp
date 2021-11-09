const functions = require('firebase-functions');
const braintree = require('braintree');
const express = require('express');
const cors = require('cors');

// Init app
const app = express();
app.use(cors({ origin: true }));

var gateway = new braintree.BraintreeGateway({
    environment: braintree.Environment.Sandbox,
    merchantId: 'r4bzmfjqv4w7mbkx',
    publicKey: '2t3m5hgbm37bcpjz',
    privateKey: 'b4523b1df7106e7ba69d219b29f0c9a8'
});

app.get('/token', (req, response) => {
    gateway.clientToken.generate({}, (err, res) => {
        if (res) {
            response.send(JSON.stringify({ error: false, token: res.clientToken }));
        } else {
            response.send(JSON.stringify({ error: true, errorObj: err, response: res }));
        }
    });
});

app.post('/checkout', (req, response) => {
    var transactionErrors;
    var amount = req.body.amount;
    var nonce = req.body.payment_method_nonce;

    gateway.transaction.sale(
        {
            amount: amount,
            paymentMethodNonce: nonce,
            options: {
                submitForSettlement: true
            }
        },
        (error, result) => {
            if (result.success || result.transaction) {
                response.send(JSON.stringify(result));
            } else {
                transactionErrors = result.errors.deepErrors();
                response.send(JSON.stringify(formatErrors(transactionErrors)));
            }
        }
    );
});

exports.widget = functions.https.onRequest(app);
