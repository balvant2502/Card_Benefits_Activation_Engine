# End-to-End Benefits Flow Test

This document records the successful flow test performed on 2026-09-20:

1. Authenticate as an administrator.
2. Authenticate as a customer.
3. Read the customer's virtual card.
4. Configure a benefit rule for that card.
5. Swipe the dummy card.
6. Evaluate transaction eligibility.
7. Display the eligible offer on the customer dashboard.
8. Create and claim the benefit.
9. Verify prefilled claim data.
10. Activate and submit the claim.
11. Verify final status, audit history, and notification.

The examples use placeholders for credentials and JWTs. Do not commit real
passwords or tokens to source control.

## Environment

```text
Base URL: http://localhost:8090
API prefix: /api
```

Use this header on protected requests:

```http
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

## 1. Administrator login

### Request

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "<ADMIN_EMAIL>",
  "password": "<ADMIN_PASSWORD>"
}
```

### Representative response

```json
{
  "userId": 39,
  "email": "testing@gmail.com",
  "name": "Admin User",
  "role": "ADMIN",
  "token": "<ADMIN_JWT_TOKEN>",
  "message": "Login successful"
}
```

Result: **PASS — HTTP 200**

## 2. Customer login

### Request

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "<CUSTOMER_EMAIL>",
  "password": "<CUSTOMER_PASSWORD>"
}
```

### Representative response

```json
{
  "userId": 38,
  "email": "<CUSTOMER_EMAIL>",
  "name": "Copilot Test",
  "role": "CUSTOMER",
  "token": "<CUSTOMER_JWT_TOKEN>",
  "message": "Login successful"
}
```

Result: **PASS — HTTP 200**

## 3. Verify the authenticated customer

### Request

```http
GET /api/auth/me
Authorization: Bearer <CUSTOMER_JWT_TOKEN>
```

### Representative response

```json
{
  "userId": 38,
  "email": "<CUSTOMER_EMAIL>",
  "name": "Copilot Test",
  "role": "CUSTOMER",
  "token": null,
  "message": "User found"
}
```

Result: **PASS — HTTP 200**

## 4. Load the customer's cards

### Request

```http
GET /api/cards/me
Authorization: Bearer <CUSTOMER_JWT_TOKEN>
```

### Representative response

```json
[
  {
    "id": 7,
    "last4": "1234",
    "cardType": "VIRTUAL_DEMO",
    "network": "VISA",
    "active": true,
    "virtual": true,
    "provider": "MOCK",
    "benefits": [
      "Purchase Protection"
    ]
  }
]
```

The `id` value is used as `<CARD_ID>` in subsequent requests.

Result: **PASS — HTTP 200**

## 5. Read available benefits as administrator

### Request

```http
GET /api/benefits
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

### Representative response

```json
[
  {
    "id": 6,
    "name": "Purchase Protection Plus",
    "description": "Updated coverage for eligible purchases.",
    "applicableCategories": "RETAIL,ELECTRONICS",
    "minAmount": 0,
    "requiredClaimFields": "merchant,amount,date,category",
    "benefitType": "PURCHASE_PROTECTION",
    "isActive": true
  }
]
```

The `id` value is used as `<BENEFIT_ID>`.

Result: **PASS — HTTP 200**

## 6. Assign the benefit to the customer card

### Request

```http
POST /api/cards/<CARD_ID>/benefits/<BENEFIT_ID>
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

This endpoint has no request body.

### Representative response

```json
{
  "id": 3,
  "card": {
    "id": 7
  },
  "benefit": {
    "id": 6,
    "name": "Purchase Protection Plus"
  },
  "isActive": true
}
```

Result: **PASS — HTTP 200**

If the benefit is already assigned, the service rejects the duplicate
assignment. In that case, continue using the existing card-benefit mapping.

## 7. Create the active eligibility rule

### Request

```http
POST /api/cards/<CARD_ID>/benefits/<BENEFIT_ID>/rules
Authorization: Bearer <ADMIN_JWT_TOKEN>
Content-Type: application/json
```

```json
{
  "category": "",
  "minAmount": 0,
  "priority": 100,
  "isActive": true,
  "description": "E2E active broad rule"
}
```

Blank category, card type, card network, and merchant keywords match any value.

### Representative response

```json
{
  "id": 3,
  "benefit": {
    "id": 6,
    "name": "Purchase Protection Plus"
  },
  "cardBenefit": {
    "id": 3
  },
  "category": "",
  "minAmount": 0,
  "maxAmount": null,
  "merchantKeywords": null,
  "cardType": null,
  "cardNetwork": null,
  "priority": 100,
  "isActive": true,
  "description": "E2E active broad rule"
}
```

Result: **PASS — HTTP 200**

The rule entity was updated with a `@PrePersist` lifecycle hook so
`createdAt` and `updatedAt` are initialized when a rule is created.

## 8. Perform a dummy card swipe

### Request

```http
POST /api/transactions/dummy-swipe
Authorization: Bearer <CUSTOMER_JWT_TOKEN>
Content-Type: application/json
```

```json
{
  "cardId": 7
}
```

### Representative response

```json
{
  "id": 3,
  "merchant": "Reliance Retail",
  "category": "RETAIL",
  "amount": 7408,
  "txnDate": "2026-09-20T15:08:10.699852300",
  "isEligible": true,
  "eligibleBenefitName": "Purchase Protection Plus",
  "eligibilityReason": "Reliance Retail qualifies for Purchase Protection Plus (Amount: 7408, Category: RETAIL)"
}
```

Result: **PASS — HTTP 200**

The response `id` is used as `<TRANSACTION_ID>`.

## 9. Check eligibility and dashboard offer

### Request

```http
GET /api/eligibility/<TRANSACTION_ID>
Authorization: Bearer <CUSTOMER_JWT_TOKEN>
```

### Representative response

```json
{
  "transactionId": 3,
  "isEligible": true,
  "benefitId": 6,
  "benefitName": "Purchase Protection Plus",
  "reason": "Reliance Retail qualifies for Purchase Protection Plus (Amount: 7408, Category: RETAIL)"
}
```

Result: **PASS — HTTP 200**

The customer dashboard can also load the transaction and offer through:

```http
GET /api/transactions/user/38
Authorization: Bearer <CUSTOMER_JWT_TOKEN>
```

Result: **PASS — HTTP 200**

## 10. Claim the eligible offer

### Request

```http
POST /api/claims?transactionId=<TRANSACTION_ID>&benefitId=<BENEFIT_ID>&userId=<CUSTOMER_ID>
Authorization: Bearer <CUSTOMER_JWT_TOKEN>
```

This endpoint has no request body.

### Representative response

```json
{
  "id": 1,
  "transaction": {
    "id": 3
  },
  "benefit": {
    "id": 6,
    "name": "Purchase Protection Plus"
  },
  "user": {
    "id": 38
  },
  "status": "ELIGIBLE",
  "prefilledData": "{\"merchant\":\"Reliance Retail\",\"amount\":7408,\"date\":\"2026-09-20T15:08:10.699852300\",\"category\":\"RETAIL\"}",
  "submissionData": null,
  "adminNotes": null
}
```

Result: **PASS — HTTP 200**

## 11. Retrieve the claim and verify prefilled data

### Request

```http
GET /api/claims/<CLAIM_ID>
Authorization: Bearer <CUSTOMER_JWT_TOKEN>
```

Result: **PASS — HTTP 200**

The `prefilledData` field was present and contained merchant, amount, date,
and category values copied from the transaction.

Decoded `prefilledData`:

```json
{
  "merchant": "Reliance Retail",
  "amount": 7408,
  "date": "2026-09-20T15:08:10.699852300",
  "category": "RETAIL"
}
```

## 12. Activate the claim

### Request

```http
PUT /api/claims/<CLAIM_ID>/activate
Authorization: Bearer <CUSTOMER_JWT_TOKEN>
```

### Representative response

```json
{
  "id": 1,
  "status": "ACTIVATED",
  "prefilledData": "{\"merchant\":\"Reliance Retail\",\"amount\":7408,\"date\":\"2026-09-20T15:08:10.699852300\",\"category\":\"RETAIL\"}"
}
```

Result: **PASS — HTTP 200**

## 13. Submit the claim

### Request

```http
PUT /api/claims/<CLAIM_ID>/submit
Authorization: Bearer <CUSTOMER_JWT_TOKEN>
Content-Type: application/json
```

```json
{
  "submissionData": "{\"merchant\":\"Reliance Retail\",\"amount\":7408,\"date\":\"2026-09-20T15:08:10.699852300\",\"category\":\"RETAIL\"}"
}
```

### Representative response

```json
{
  "id": 1,
  "status": "SUBMITTED",
  "prefilledData": "{\"merchant\":\"Reliance Retail\",\"amount\":7408,\"date\":\"2026-09-20T15:08:10.699852300\",\"category\":\"RETAIL\"}",
  "submissionData": "{\"merchant\":\"Reliance Retail\",\"amount\":7408,\"date\":\"2026-09-20T15:08:10.699852300\",\"category\":\"RETAIL\"}"
}
```

Result: **PASS — HTTP 200**

## 14. Verify claim status and audit history

### Final claim request

```http
GET /api/claims/<CLAIM_ID>
Authorization: Bearer <CUSTOMER_JWT_TOKEN>
```

Final status observed: **`SUBMITTED`**

### History request

```http
GET /api/claims/<CLAIM_ID>/history
Authorization: Bearer <CUSTOMER_JWT_TOKEN>
```

Expected status transitions:

```json
[
  {
    "statusFrom": null,
    "statusTo": "ELIGIBLE",
    "actionBy": "SYSTEM",
    "remarks": "Claim created and eligible"
  },
  {
    "statusFrom": "ELIGIBLE",
    "statusTo": "ACTIVATED",
    "actionBy": "CUSTOMER",
    "remarks": "Claim activated by customer"
  },
  {
    "statusFrom": "ACTIVATED",
    "statusTo": "SUBMITTED",
    "actionBy": "CUSTOMER",
    "remarks": "Claim submitted for review"
  }
]
```

Result: **PASS — 3 history entries**

## 15. Verify claim notification

### Notification list

```http
GET /api/claim-notifications/user/<CUSTOMER_ID>
Authorization: Bearer <CUSTOMER_JWT_TOKEN>
```

### Representative response

```json
[
  {
    "id": 1,
    "claimId": 1,
    "benefitName": "Purchase Protection Plus",
    "message": "You may claim Purchase Protection Plus for your Reliance Retail transaction.",
    "read": false,
    "createdAt": "2026-09-20T15:08:10.725"
  }
]
```

Result: **PASS — notification found for the claim**

### Unread count

```http
GET /api/claim-notifications/user/<CUSTOMER_ID>/unread-count
Authorization: Bearer <CUSTOMER_JWT_TOKEN>
```

### Representative response

```json
1
```

Result: **PASS — HTTP 200**

## Final result

| Stage | Result |
| --- | --- |
| Administrator authentication | PASS |
| Customer authentication | PASS |
| Customer card dashboard | PASS |
| Benefit assignment | PASS |
| Eligibility rule creation | PASS |
| Dummy swipe and transaction creation | PASS |
| Eligibility evaluation | PASS |
| Offer shown on dashboard | PASS |
| Claim creation | PASS |
| Prefilled claim data | PASS |
| Claim activation | PASS |
| Claim submission | PASS |
| Final status verification | PASS — `SUBMITTED` |
| Audit history | PASS — 3 entries |
| Claim notification | PASS |
