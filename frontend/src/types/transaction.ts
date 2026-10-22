export interface Transaction {
  id?: number;
  amount: number;
  commerce: string;
  clientName: string;
  transactionDate: string; // Llega como string en formato ISO 8601
}