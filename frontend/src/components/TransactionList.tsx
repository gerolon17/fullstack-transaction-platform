import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../api/axiosConfig';
import type { Transaction } from '../types/transaction';

/**
 * Asynchronously fetches the complete list of transactions from the backend API.
 * 
 * @returns {Promise<Transaction[]>} A promise that resolves to an array of Transaction records.
 */
const fetchTransactions = async (): Promise<Transaction[]> => {
  const response = await apiClient.get('/transaction');
  return response.data;
};

/**
 * React functional component responsible for rendering the transaction history.
 * <p>
 * This component leverages {@code @tanstack/react-query} to handle data fetching, 
 * in-memory caching, loading state management, and error handling. It conditionally 
 * renders a loading indicator, an error message, an empty state, or a populated 
 * data table based on the current query lifecycle.
 * </p>
 *
 * @returns A structured UI component displaying the list of transactions.
 */
export const TransactionList = () => {
  // React Query automatically handles caching, background refetching, and state management
  const { data: transactions, isLoading, isError, error } = useQuery({
    queryKey: ['transactions'], // Unique cache identifier used for invalidation and refetching
    queryFn: fetchTransactions,
  });

  if (isLoading) return <p>Loading transactions...</p>;
  if (isError) return <p>Error loading data: {(error as Error).message}</p>;

  return (
    <div className="card">
      <h2 className="card-title">Transaction History</h2>
      
      {transactions?.length === 0 ? (
        <p className="empty-state">No transactions found. Create your first one above!</p>
      ) : (
        <div className="table-container">
          <table className="modern-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Client</th>
                <th>Commerce</th>
                <th>Amount</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              {transactions?.map((tx) => (
                <tr key={tx.id}>
                  <td>#{tx.id}</td>
                  <td>{tx.clientName}</td>
                  <td>{tx.commerce}</td>
                  <td className="amount-cell">${tx.amount.toLocaleString()}</td>
                  <td>{new Date(tx.transactionDate).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};