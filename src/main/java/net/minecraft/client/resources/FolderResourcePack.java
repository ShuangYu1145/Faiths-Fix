package net.minecraft.client.resources;

import com.google.common.collect.Sets;

import java.io.*;
import java.util.Set;
import org.apache.commons.io.filefilter.DirectoryFileFilter;

public class FolderResourcePack extends AbstractResourcePack
{
    public FolderResourcePack(File resourcePackFileIn)
    {
        super(resourcePackFileIn);
    }

    protected InputStream getInputStreamByName(String name) throws IOException
    {
        return new BufferedInputStream(new FileInputStream(new File(this.resourcePackFile, name)));
    }

    protected boolean hasResourceName(String name)
    {
        return (new File(this.resourcePackFile, name)).isFile();
    }

    public Set<String> getResourceDomains()
    {
        Set<String> set = Sets.<String>newHashSet();
        File file1 = new File(this.resourcePackFile, "assets/");

        if (file1.isDirectory())
        {
            for (File file2 : file1.listFiles((FilenameFilter) DirectoryFileFilter.DIRECTORY))
            {
                String s = getRelativeName(file1, file2);

                if (!s.equals(s.toLowerCase()))
                {
                    this.logNameNotLowercase(s);
                }
                else
                {
                    set.add(s.substring(0, s.length() - 1));
                }
            }
        }

        return set;
    }
}
