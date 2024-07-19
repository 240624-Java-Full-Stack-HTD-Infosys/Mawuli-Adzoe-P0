import React, { useState, useContext } from 'react';
import UserContext from './UserProvider';

const BankAccountCreationForm = ({ handleClose }) => {
    const { createBankAccount } = useContext(UserContext);
    const [formData, setFormData] = useState({ accountType: '', email: '' });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prevState => ({
            ...prevState,
            [name]: value
        }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        createBankAccount(formData);
        handleClose();
    };

    return (
        <form onSubmit={handleSubmit}>
            <h3>Create Bank Account</h3>
            <label>
                Account Type:
                <input type="text" name="accountType" value={formData.accountType} onChange={handleChange} />
            </label>
            <label>
                Email:
                <input type="email" name="email" value={formData.email} onChange={handleChange} />
            </label>
            <button type="submit">Create Account</button>
            <button type="button" onClick={handleClose}>Cancel</button>
        </form>
    );
};

export default BankAccountCreationForm;
