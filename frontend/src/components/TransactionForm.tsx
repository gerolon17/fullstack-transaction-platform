import { useForm } from 'react-hook-form';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/axiosConfig';
import type { Transaction } from '../types/transaction';
import { useState } from 'react';
import '../index.css'; 

/**
 * Type definition for the transaction form payload.
 * <p>
 * Omits the 'id' field from the base {@link Transaction} interface, 
 * as the unique identifier is auto-generated and assigned by the backend database 
 * upon successful persistence.
 * </p>
 */
type TransactionFormData = Omit<Transaction, 'id'>;

/**
 * React functional component responsible for capturing and submitting new transaction data.
 * <p>
 * This component integrates:
 * - {@code react-hook-form} for robust, client-side validation and controlled input management.
 * - {@code @tanstack/react-query} for asynchronous API mutations, error state handling, 
 *   and cache invalidation (which triggers automatic UI updates on the transaction list).
 * </p>
 * 
 * @returns A structured and styled form UI for transaction entry.
 */
export const TransactionForm = () => {
  const queryClient = useQueryClient();
  const [apiError, setApiError] = useState<string | null>(null);

  /**
   * Initializes react-hook-form for client-side validation.
   */
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<TransactionFormData>();

  /**
   * React Query mutation to handle the asynchronous POST request to the Spring Boot backend.
   * <p>
   * On success, it clears the form and invalidates the 'transactions' cache to seamlessly 
   * refresh the data table. On error, it parses the HTTP status codes to display 
   * user-friendly messages for rate limiting (429) or business rule violations (400).
   * </p>
   */
  const mutation = useMutation({
    mutationFn: async (newTx: TransactionFormData) => {
      const response = await apiClient.post('/transaction', newTx);
      return response.data;
    },
    onSuccess: () => {
      // Clear the form and any previous errors upon successful persistence
      reset();
      setApiError(null);
      // Invalidate the cache to trigger an automatic background re-fetch of the transaction list
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
    },
    onError: (error: any) => {
      // Handle HTTP 429 (Rate Limit) or HTTP 400 (Business Rules / Validation) gracefully
      if (error.response?.status === 429) {
        setApiError('Rate limit exceeded (Max 3 requests per minute). Please wait a moment.');
      } else if (error.response?.data?.message) {
        // Displays the custom 100-transaction limit business rule error returned by the API
        setApiError(error.response.data.message); 
      } else {
        setApiError('An unexpected error occurred while saving the transaction.');
      }
    },
  });

  /**
   * Submit handler invoked only if all client-side validation rules pass.
   * 
   * @param data The validated transaction payload ready for API submission.
   */
  const onSubmit = (data: TransactionFormData) => {
    mutation.mutate(data);
  };

  /**
   * Calculates the current date and time in YYYY-MM-DDTHH:mm format.
   * Utilized to restrict the HTML datetime-local input, preventing future date selection.
   */
  const todayMax = new Date().toISOString().slice(0, 16);

  return (
    <div className="card">
      <h3 className="card-title">New Transaction</h3>
      
      {apiError && <div className="alert-error">{apiError}</div>}

      <form onSubmit={handleSubmit(onSubmit)} className="form-grid">
        
        <div className="input-group">
          <label>Client Name</label>
          <input 
            type="text" 
            className="input-field"
            placeholder="e.g. John Doe"
            {...register('clientName', { required: 'Client name is required' })} 
          />
          {errors.clientName && <span className="error-text">{errors.clientName.message}</span>}
        </div>

        <div className="input-group">
          <label>Commerce</label>
          <input 
            type="text" 
            className="input-field"
            placeholder="e.g. Amazon"
            {...register('commerce', { required: 'Commerce is required' })} 
          />
          {errors.commerce && <span className="error-text">{errors.commerce.message}</span>}
        </div>

        <div className="input-group">
          <label>Amount (USD)</label>
          <input 
            type="number" 
            className="input-field"
            placeholder="0.00"
            {...register('amount', { 
              required: 'Amount is required',
              min: { value: 0, message: 'Amount cannot be negative' },
              valueAsNumber: true // Automatically parses the input string into a valid number
            })} 
          />
          {errors.amount && <span className="error-text">{errors.amount.message}</span>}
        </div>

        <div className="input-group">
          <label>Date (Max today)</label>
          <input 
            type="datetime-local" 
            max={todayMax}
            className="input-field"
            {...register('transactionDate', { required: 'Date is required' })} 
          />
          {errors.transactionDate && <span className="error-text">{errors.transactionDate.message}</span>}
        </div>

        <div className="btn-submit-container">
          <button 
            type="submit" 
            disabled={mutation.isPending}
            className="btn-primary"
          >
            {mutation.isPending ? 'Processing...' : 'Add Transaction'}
          </button>
        </div>
      </form>
    </div>
  );
};