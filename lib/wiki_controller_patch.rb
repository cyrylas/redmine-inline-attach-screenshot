module AttachScreenshotPlugin
  module WikiControllerPatch
    def self.included(base)
      # If the user is running an older version of Redmine we're not going to do this...
      if Redmine::VERSION::MAJOR == 1 and Redmine::VERSION::MINOR < 3 then
        return
      end
      base.class_eval do
        # This method needs to be overridden when editing a wiki page, adding a file, but not editing content.
        def update_with_plugin
          page_new_record = @page.new_record?
          ret = update_without_plugin          
          if !page_new_record && params[:content].present? && @content.text == params[:content][:text]
            call_hook(:controller_wiki_edit_after_save, { :params => params, :page => @page})
          end
          return ret
        end
        alias_method_chain :update, :plugin

        # This method needs to be overridden for "new file" attachments at the bottom of a wiki page.
        def add_attachment_with_plugin
          ret = add_attachment_without_plugin
          call_hook(:controller_wiki_edit_after_save, { :params => params, :page => @page})
          return ret
        end
        alias_method_chain :add_attachment, :plugin

      end
    end
  end
end