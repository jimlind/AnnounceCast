type DictionaryData = {
    [key: string]: string;
};

export class CacheDictionary {
    defaultValue: string;
    constructor(defaultValue: string) {
        this.defaultValue = defaultValue;
    }

    private data: DictionaryData = {};
    set(key: string, value: string) {
        this.data[key] = value;
    }
    get(key: string): string {
        return this.data[key] || this.defaultValue;
    }
}
