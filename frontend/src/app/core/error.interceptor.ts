import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

// Problem detail as returned by the backend
interface ProblemDetail {
  title?: string;
  detail?: string;
  status?: number;
}

export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string
  ) {
    super(message);
  }
}

// Turns every failure into one readable message, so components stop mapping status codes
export const errorInterceptor: HttpInterceptorFn = (req, next) =>
  next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      let message: string;

      if (err.status === 0) {
        message = 'No connection to the server.';
      } else {
        const problem = err.error as ProblemDetail | null;
        message = problem?.detail || problem?.title || defaultMessage(err.status);
      }

      return throwError(() => new ApiError(err.status, message));
    })
  );

function defaultMessage(status: number): string {
  switch (status) {
    case 400:
      return 'The request was invalid.';
    case 401:
      return 'Please sign in to continue.';
    case 403:
      return 'You are not allowed to do that.';
    case 404:
      return 'Not found.';
    case 409:
      return 'That conflicts with the current state.';
    case 502:
    case 503:
      return 'The service is temporarily unavailable.';
    default:
      return 'Something went wrong.';
  }
}
