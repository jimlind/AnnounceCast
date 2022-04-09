type DictionaryData = {
    [key: string]: string[];
};

export class CacheDictionary {
    limit: number;
    constructor(limit: number) {
        this.limit = limit;
    }

    private data: DictionaryData = {};

    add(key: string, value: string): string[] {
        const result = [...this.get(key), value].slice(this.limit * -1);
        this.data[key] = result;

        return result;
    }

    get(key: string): string[] {
        return this.data[key] || [];
    }

    getAllKeys(): string[] {
        return Object.keys(this.data);
    }

    count(): number {
        return this.getAllKeys().length;
    }
}
