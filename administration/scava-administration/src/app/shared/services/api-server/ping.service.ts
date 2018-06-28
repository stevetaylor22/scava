import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { LocalStorageService } from '../authentication/local-storage.service';

@Injectable({
    providedIn: 'root'
})
export class PingService {

    private resourceUrl = environment.SERVER_API_URL + 'administration';
    private ping = 'ping';
    private jwtToken: string = null;

    constructor(
        private httpClient: HttpClient,
        private localStorageService: LocalStorageService
    ) { }

    testPing() {
        if (this.jwtToken == null) {
            this.jwtToken = this.localStorageService.loadToken();
        }
        console.log(`${this.resourceUrl}/${this.ping}`);
        return this.httpClient.get(`${this.resourceUrl}/${this.ping}`,
            { headers: new HttpHeaders({ 'Authorization': this.jwtToken }) });
    }
}
