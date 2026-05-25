import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, switchMap, throwError } from 'rxjs';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getAccessToken();
  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((err) => {
      const isAuthEndpoint = req.url.includes('/api/auth/');
      const hasBackendMessage = !!err?.error?.error;
      const canRefresh = authService.hasRefreshToken();

      if ((err.status === 401 || err.status === 403) && !isAuthEndpoint && !hasBackendMessage && canRefresh) {
        return authService.refresh().pipe(
          switchMap((res) => next(req.clone({
            setHeaders: { Authorization: `Bearer ${res.accessToken}` }
          }))),
          catchError((refreshErr) => {
            authService.logout();
            return throwError(() => refreshErr);
          })
        );
      }

      return throwError(() => err);
    })
  );
};
