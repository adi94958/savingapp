# Savings Management API Contract

This document describes the API specification for the **Savings Management System**, with two roles: **STAFF** and **USER**.

---

## Global Conventions

- **Base URL**: `/api/v1`
- **Auth**: `Authorization: Bearer <accessToken>` (JWT)
- **Dates**: `YYYY-MM-DD`; timestamps use ISO-8601 UTC
- **Sorting**: `?sort=field,asc|desc` (multi-sort supported)
- **Pagination**: 1-based → `?page=1&perPage=10`

### ✅ Success Responses

**Detail**

```json
{
  "message": "Data retrieved successfully",
  "data": {
    /* object */
  }
}
```

**List with pagination**

```json
{
  "message": "Account list retrieved successfully",
  "data": [
    /* rows */
  ],
  "pagination": { "currentPage": 1, "lastPage": 5, "perPage": 10, "total": 49 }
}
```

**POST/PUT/DELETE**

```json
{
  "message": "Account created successfully",
  "data": {
    /* object or null */
  }
}
```

### ❌ Error Responses

```json
{ "message": "An error occurred", "errorMessage": "Clear error description" }
```

**Common HTTP Status Codes**

- `200 OK`
- `201 Created`
- `400 Bad Request`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`
- `409 Conflict` (e.g., inactive account)
- `422 Unprocessable Entity` (validation, insufficient balance)
- `500 Internal Server Error`

---

## Entities

### User

- `id: UUID`
- `fullName: string`
- `email: string`
- `role: "USER" | "STAFF"`
- `address: text`
- `phone: string`
- `createdAt: ISO-8601`

### Account

- `accountCode: string` (unique, human-facing; e.g., `ACC-0001`)
- `userId: UUID`
- `accountName: string` (STAFF views)
- `name: string`
- `isActive: boolean`
- `totalDeposit: number` (computed)
- `totalWithdraw: number` (computed)
- `balance: number`

### Transaction

- `id: UUID`
- `accountCode: string`
- `type: "DEPOSIT" | "WITHDRAW"`
- `amount: number (>0)`
- `occurredAt: date`
- `note?: string`
- `createdAt: ISO-8601`

---

## 1. Auth

### POST `/auth/login`

**Request**

```json
{ "email": "adi@mail.com", "password": "Secret@123" }
```

**200**

```json
{
  "message": "Login successful",
  "data": {
    "accessToken": "jwt_access_here",
    "refreshToken": "jwt_refresh_here",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": "uuid",
      "fullName": "Muhammad Adi",
      "email": "adi@mail.com",
      "role": "STAFF"
    }
  }
}
```

**401**

```json
{ "message": "An error occurred", "errorMessage": "Invalid email or password" }
```

---

### POST `/auth/refresh`

**Request**

```json
{ "refreshToken": "jwt_refresh_here" }
```

**200**

```json
{
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "jwt_access_new",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

---

### GET `/auth/me`

**200**

```json
{
  "message": "Data retrieved successfully",
  "data": {
    "id": "uuid",
    "fullName": "Muhammad Adi",
    "email": "adi@mail.com",
    "role": "USER"
  }
}
```

---

## 2. STAFF — Dashboard

### GET `/staff/dashboard?from=YYYY-MM-DD&to=YYYY-MM-DD`

**200**

```json
{
  "message": "Dashboard retrieved successfully",
  "data": {
    "period": { "from": "2025-09-01", "to": "2025-09-30" },
    "users": { "total": 230 },
    "accounts": { "total": 180, "active": 170, "inactive": 10 },
    "transactions": {
      "totalDeposit": 135000000,
      "totalWithdraw": 67000000,
      "net": 68000000
    },
    "topAccountsByDeposit": [
      {
        "accountCode": "ACC-0012",
        "accountName": "Agus",
        "totalDeposit": 12000000
      }
    ]
  }
}
```

---

## 3. STAFF — Users

### GET `/users?page=1&perPage=10&sort=fullName,asc&q=adi`

**200**

```json
{
  "message": "User list retrieved successfully",
  "data": [
    {
      "id": "uuid",
      "fullName": "Muhammad Adi",
      "email": "adi@mail.com",
      "role": "USER",
      "createdAt": "2025-09-10T01:02:03Z"
    }
  ],
  "pagination": { "currentPage": 1, "lastPage": 1, "perPage": 10, "total": 1 }
}
```

---

### GET `/users/{userId}`

**200**

```json
{
  "message": "Data retrieved successfully",
  "data": {
    "id": "uuid",
    "fullName": "Muhammad Adi",
    "email": "adi@mail.com",
    "role": "USER",
    "createdAt": "2025-09-10T01:02:03Z"
  }
}
```

---

### POST `/users`

**Request**

```json
{
  "fullName": "Muhammad Adi",
  "email": "adi@mail.com",
  "password": "Secret@123",
  "role": "USER"
}
```

**201**

```json
{
  "message": "User created successfully",
  "data": {
    "id": "uuid",
    "fullName": "Muhammad Adi",
    "email": "adi@mail.com",
    "role": "USER"
  }
}
```

---

### PUT `/users/{userId}`

**Request**

```json
{ "fullName": "M. Adi Saputera", "email": "adi@mail.com", "role": "USER" }
```

**200**

```json
{
  "message": "User updated successfully",
  "data": {
    "id": "uuid",
    "fullName": "M. Adi Saputera",
    "email": "adi@mail.com",
    "role": "USER"
  }
}
```

---

### PATCH `/users/{userId}/password`

**Request**

```json
{ "newPassword": "NewPassword@123" }
```

**200**

```json
{ "message": "Password reset successfully", "data": null }
```

---

### DELETE `/users/{userId}`

**200**

```json
{ "message": "User deleted successfully", "data": null }
```

---

## 4. STAFF — Accounts

### GET `/accounts?page=1&perPage=10&sort=createdAt,desc&q=ACC-0001&active=true`

**200**

```json
{
  "message": "Account list retrieved successfully",
  "data": [
    {
      "accountCode": "ACC-0001",
      "name": "Education Account",
      "userId": "uuid-user",
      "accountName": "Muhammad Adi",
      "isActive": true,
      "totalDeposit": 850000,
      "totalWithdraw": 300000,
      "balance": 550000
    }
  ],
  "pagination": { "currentPage": 1, "lastPage": 1, "perPage": 10, "total": 1 }
}
```

---

### GET `/accounts/{accountCode}`

**200**

```json
{
  "message": "Data retrieved successfully",
  "data": {
    "accountCode": "ACC-0001",
    "name": "Education Account",
    "userId": "uuid-user",
    "accountName": "Muhammad Adi",
    "isActive": true,
    "totalDeposit": 850000,
    "totalWithdraw": 300000,
    "balance": 550000
  }
}
```

---

### POST `/accounts`

**Request**

```json
{
  "userId": "uuid-user",
  "accountCode": "ACC-0001",
  "name": "Education Account"
}
```

**201**

```json
{
  "message": "Account created successfully",
  "data": {
    "accountCode": "ACC-0001",
    "name": "Education Account",
    "balance": 0
  }
}
```

---

### PUT `/accounts/{accountCode}`

**Request**

```json
{ "name": "Child Education Account" }
```

**200**

```json
{
  "message": "Account updated successfully",
  "data": {
    "accountCode": "ACC-0001",
    "name": "Child Education Account",
    "balance": 550000
  }
}
```

---

### PATCH `/accounts/{accountCode}/status`

**Request**

```json
{ "isActive": false }
```

**200**

```json
{
  "message": "Account status updated successfully",
  "data": { "accountCode": "ACC-0001", "isActive": false, "balance": 0 }
}
```

---

### DELETE `/accounts/{accountCode}`

**200**

```json
{ "message": "Account deleted successfully", "data": null }
```

---

### GET `/accounts/{accountCode}/transactions?page=1&perPage=20&sort=occurredAt,desc&type=DEPOSIT&from=2025-09-01&to=2025-09-30&q=note`

**200**

```json
{
  "message": "Transaction list retrieved successfully",
  "data": [
    {
      "id": "tx-uuid",
      "accountCode": "ACC-0001",
      "type": "DEPOSIT",
      "amount": 150000,
      "occurredAt": "2025-09-13",
      "note": "Monthly saving",
      "createdAt": "2025-09-13T02:11:00Z"
    }
  ],
  "pagination": { "currentPage": 1, "lastPage": 1, "perPage": 20, "total": 5 }
}
```

---

## 5. STAFF — Transactions

### POST `/transactions`

**Request**

```json
{
  "accountCode": "ACC-0001",
  "type": "DEPOSIT",
  "amount": 150000,
  "occurredAt": "2025-09-13",
  "note": "Monthly saving"
}
```

**201**

```json
{
  "message": "Transaction created successfully",
  "data": {
    "id": "tx-uuid",
    "accountCode": "ACC-0001",
    "type": "DEPOSIT",
    "amount": 150000,
    "occurredAt": "2025-09-13",
    "note": "Monthly saving",
    "createdAt": "2025-09-13T02:11:00Z"
  }
}
```

**409**

```json
{ "message": "An error occurred", "errorMessage": "Account is inactive" }
```

**422**

```json
{ "message": "An error occurred", "errorMessage": "Insufficient balance" }
```

---

### PUT `/transactions/{transactionId}`

**Request**

```json
{
  "type": "DEPOSIT",
  "amount": 175000,
  "occurredAt": "2025-09-13",
  "note": "Revised deposit"
}
```

**200**

```json
{
  "message": "Transaction updated successfully",
  "data": {
    "id": "tx-uuid",
    "accountCode": "ACC-0001",
    "type": "DEPOSIT",
    "amount": 175000,
    "occurredAt": "2025-09-13",
    "note": "Revised deposit",
    "createdAt": "2025-09-13T02:11:00Z"
  }
}
```

---

### DELETE `/transactions/{transactionId}`

**200**

```json
{ "message": "Transaction deleted successfully", "data": null }
```

---

### GET `/transactions?accountCode=ACC-0001&page=1&perPage=20&sort=occurredAt,desc&type=WITHDRAW&from=2025-09-01&to=2025-09-30&q=note`  

**200**

```json
{
  "message": "Transaction list retrieved successfully",
  "data": [
    /* rows */
  ],
  "pagination": { "currentPage": 1, "lastPage": 1, "perPage": 20, "total": 5 }
}
```

---

## 6. USER — Read Only

### GET `/me/dashboard`

**200**

```json
{
  "message": "Data retrieved successfully",
  "data": {
    "totalBalance": 3250000,
    "accounts": [
      { "accountCode": "ACC-0001", "name": "Main", "balance": 2000000 },
      { "accountCode": "ACC-0002", "name": "Emergency", "balance": 1250000 }
    ],
    "thisMonth": {
      "period": "2025-09",
      "totalDeposit": 850000,
      "totalWithdraw": 300000
    },
    "recentTransactions": [
      {
        "id": "tx-uuid",
        "accountCode": "ACC-0001",
        "type": "DEPOSIT",
        "amount": 150000,
        "occurredAt": "2025-09-13",
        "note": "Monthly saving"
      }
    ]
  }
}
```

---

### GET `/me/accounts?page=1&perPage=10&sort=createdAt,desc`

**200**

```json
{
  "message": "Account list retrieved successfully",
  "data": [
    { "accountCode": "ACC-0001", "name": "Main", "balance": 2000000 },
    { "accountCode": "ACC-0002", "name": "Emergency", "balance": 1250000 }
  ],
  "pagination": { "currentPage": 1, "lastPage": 1, "perPage": 10, "total": 2 }
}
```

---

### GET `/me/accounts/{accountCode}`

**200**

```json
{
  "message": "Data retrieved successfully",
  "data": { "accountCode": "ACC-0001", "name": "Main", "balance": 2000000 }
}
```

---

### GET `/me/accounts/{accountCode}/transactions?page=1&perPage=20&sort=occurredAt,desc&type=DEPOSIT&from=...&to=...`

**200**

```json
{
  "message": "Transaction list retrieved successfully",
  "data": [
    /* user-owned, active accounts only */
  ],
  "pagination": { "currentPage": 1, "lastPage": 3, "perPage": 20, "total": 57 }
}
```

---

## Business Rules

- Account status controlled by `isActive`.
  New transactions on inactive accounts → **409 Conflict** (`"Account is inactive"`).
- Prevent negative balance on `WITHDRAW` → **422 Unprocessable Entity** (`"Insufficient balance"`).
- `amount > 0`.
- STAFF = full management access.
  USER = read-only, restricted to **own active accounts**.
- All list endpoints return the **pagination** object.
