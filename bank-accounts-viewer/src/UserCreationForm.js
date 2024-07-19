import React, { useState, useContext } from 'react';
import UserContext from './UserProvider';

const UserCreationForm = ({ handleClose }) => {
    const { registerUser } = useContext(UserContext);
    const [formData, setFormData] = useState({ name: '', email: '', phone: '', password: '', isAdmin: false });

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prevState => ({
            ...prevState,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        registerUser(formData);
        handleClose();
    };

    return (
        <form onSubmit={handleSubmit}>
            <h3>Create User</h3>
            <label>
                Name:
                <input type="text" name="name" value={formData.name} onChange={handleChange} />
            </label>
            <label>
                Email:
                <input type="email" name="email" value={formData.email} onChange={handleChange} />
            </label>
            <label>
                Phone:
                <input type="text" name="phone" value={formData.phone} onChange={handleChange} />
            </label>
            <label>
                Password:
                <input type="password" name="password" value={formData.password} onChange={handleChange} />
            </label>
            <label>
                Admin:
                <input type="checkbox" name="isAdmin" checked={formData.isAdmin} onChange={handleChange} />
                                                                                                       </label>
                                                                                                       <button type="submit">Create User</button>
                                                                                                       <button type="button" onClick={handleClose}>Cancel</button>
                                                                                                   </form>
                                                                                               );
                                                                                           };

                                                                                           export default UserCreationForm;
