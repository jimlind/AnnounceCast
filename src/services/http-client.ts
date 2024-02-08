interface HttpClientInterface {
    readonly axios: import('axios').AxiosInstance;
    get(url: string, timeout: number): Promise<import('axios').AxiosResponse>;
}

export default class HttpClient {
    readonly headers = { 'User-Agent': 'AnnounceCast Cient' };

    constructor(readonly axios: import('axios').AxiosInstance) {}

    /**
     * @param {string} url
     * @param {number} timeout
     *
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    async get(url: string, timeout: number): Promise<import('axios').AxiosResponse> {
        const axiosParams = { signal: AbortSignal.timeout(timeout), headers: this.headers };
        return await this.axios.get(url, axiosParams);
    }
}
