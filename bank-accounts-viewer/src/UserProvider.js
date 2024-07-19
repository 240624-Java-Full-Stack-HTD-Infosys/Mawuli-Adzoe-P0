import React, { useState, useEffect, createContext } from 'react';
import axios from 'axios';

const axiosInstance = axios.create({
    withCredentials: true,
    baseURL: 'http://localhost:7000',
    headers: {
        'Content-Type': 'application/json'
    }
});

const UserContext = createContext();

export const UserProvider = ({ children }) => {
    const [users, setUsers] = useState([]);
    const [error, setError] = useState(null);
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const loginAndFetchUsers = async () => {
            try {
                await axiosInstance.post('/user/login', {
                    email: "admin@localhost.com",
                    password: "adminpassword"
                });

                const userResponse = await axiosInstance.get('/admin/users');
                setUsers(userResponse.data);
            } catch (error) {
                setError(error.message);
            } finally {
                setLoading(false);
            }
        };

        loginAndFetchUsers();
    }, []);

    const registerUser = async (userData) => {
        try {
            const response = await axiosInstance.post('/user/register', userData);
            setUsers([...users, response.data]);
            setMessage('User registered successfully');
        } catch (error) {
            setError(error.message);
        }
    };

    const createBankAccount = async (accountData) => {
        try {
            const response = await axiosInstance.post('/account/create', accountData);
            const updatedUsers = users.map(user =>
                user.email === accountData.email ? { ...user, bankAccounts: [...user.bankAccounts, response.data] } : user
            );
            setUsers(updatedUsers);
            setMessage('Bank account created successfully');
        } catch (error) {
            setError(error.message);
        }
    };

    const updateUser = async (userId, userData) => {
        try {
            const response = await axiosInstance.put(`/user/update/${userId}`, userData);
            setUsers(users.map(user => user.userId === userId ? response.data : user));
            setMessage('User updated successfully');
        } catch (error) {
            setError(error.message);
        }
    };

    const deposit = async (accountNumber, amount) => {
        try {
            const response = await axiosInstance.post(`/account/deposit/${accountNumber}`, { amount });
            const updatedUsers = users.map(user => ({
                ...user,
                bankAccounts: user.bankAccounts.map(account =>
                    account.accountNumber === accountNumber ? response.data : account
                )
            }));
            setUsers(updatedUsers);
            setMessage('Deposit successful');
        } catch (error) {
            setError(error.message);
        }
    };

    const withdraw = async (accountNumber, amount) => {
        try {
            const response = await axiosInstance.post(`/account/withdraw/${accountNumber}`, { amount });
            const updatedUsers = users.map(user => ({
                ...user,
                bankAccounts: user.bankAccounts.map(account =>
                    account.accountNumber === accountNumber ? response.data : account
                )
            }));
            setUsers(updatedUsers);
            setMessage('Withdrawal successful');
        } catch (error) {
            setError(error.message);
        }
    };

    const transfer = async (accountNumber, transferData) => {
        try {
            const response = await axiosInstance.post(`/account/transfer/${accountNumber}`, transferData);
            const updatedUsers = users.map(user => ({
                ...user,
                bankAccounts: user.bankAccounts.map(account =>
                    account.accountNumber === accountNumber ? response.data : account
                )
            }));
            setUsers(updatedUsers);
            setMessage('Transfer successful');
        } catch (error) {
            setError(error.message);
        }
    };

    const deleteAccount = async (accountNumber) => {
        try {
            await axiosInstance.delete(`/account/${accountNumber}`);
            setUsers(users.map(user => ({
                ...user,
                bankAccounts: user.bankAccounts.filter(account => account.accountNumber !== accountNumber)
            })));
            setMessage('Account deleted successfully');
        } catch (error) {
            setError(error.message);
        }
    };

    return (
        <UserContext.Provider value={{
            users,
            error,
            message,
            loading,
            registerUser,
            createBankAccount,
            updateUser,
            deposit,
            withdraw,
            transfer,
            deleteAccount,
            setMessage,
            setError
        }}>
            {children}
        </UserContext.Provider>
    );
};

export default UserContext;
