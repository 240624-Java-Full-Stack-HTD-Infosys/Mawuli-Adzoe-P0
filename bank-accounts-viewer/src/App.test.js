import { render, screen } from '@testing-library/react';
import App from './App';

test('renders learn react link', () => {
  render(<App />);
  const linkElement = screen.getByText(/learn react/i);
  expect(linkElement).toBeInTheDocument();
});

//admin should be able to view all accounts
//admin should be able to view all users
//admin should be able to view all transactions
// app.get("account/{account_number}", this::getAccountHandler);
// app.get("account/{account_number}", this::getAccountHandler);
// app.get("account/{account_number}", this::getAccountHandler);
