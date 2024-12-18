import { AxiosInstance, AxiosResponse } from 'axios';

interface HttpClientInterface {
    readonly axios: AxiosInstance;
    get(url: string, timeout: number): Promise<AxiosResponse>;
}

export default class HttpClient implements HttpClientInterface {
    readonly headers = { 'User-Agent': 'AnnounceCast Cient' };

    constructor(readonly axios: AxiosInstance) {}

    /**
     * @param {string} url
     * @param {number} timeout
     *
     * @returns {Promise<AxiosResponse>}
     */
    async get(url: string, timeout: number): Promise<AxiosResponse> {
        const axiosParams = { signal: AbortSignal.timeout(timeout), headers: this.headers };
        return await this.axios.get(url, axiosParams);
    }
}
